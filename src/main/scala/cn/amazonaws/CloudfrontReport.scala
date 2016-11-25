package cn.amazonaws

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URLDecoder

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.Context

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.io.BufferedSource
import com.maxmind.geoip._
import scalaj.http._

object CloudfrontReport {

  val buttonLine = 200
  
  val bucketName = "[CF_LOGS_BUCKET]"
  
  val esEndpoint = "[ES_ENDPOINT]"
  
  val s3Client:AmazonS3 = new AmazonS3Client()
  
  def indexLogToES(event: S3Event, context: Context): java.util.List[String] = {

    val cl:LookupService = new LookupService(getClass.getResource("/GeoLiteCity.dat").toExternalForm.substring(5),
      LookupService.GEOIP_MEMORY_CACHE)

    val result = event.getRecords.asScala.map(record => decodeS3Key(record.getS3.getObject.getKey)).asJava
    val key:Option[String] = result.lift(0)


    key.getOrElse(None) match {

      case objName:String =>

	val s3object:S3Object = s3Client.getObject(new GetObjectRequest(bucketName, objName))

	val bs:BufferedSource = new BufferedSource(s3object.getObjectContent())

	for (line <- bs.getLines.drop(2)) {

	  val cols         = line.split("\t").map(_.trim)
	  val created_at   = (s"${cols(0)} ${cols(1)}")
	  val c_ip         = (s"${cols(4)}")
	  val time_taken   = (s"${cols(18)}").toDouble
	  val edgeLocation = (s"${cols(2)}")

	  val target = "http://%s/logstash-%s/type1".format(esEndpoint, (s"${cols(0)}").replaceAll("-", "."))

	  time_taken match {

	    case x if x > 0 =>

	      val sc_bytes = (s"${cols(3)}").toDouble

	      (sc_bytes/time_taken)/1024 match {

		case y if y < buttonLine =>

                  val l:Location = cl.getLocation(c_ip)

		  val doc = """{ "created_at": "%s", "location": "%s, %s", "download_speed": %f, "country": "%s", "edge_location": "%s" }""".format(
		    created_at, l.latitude, l.longitude, y, l.countryCode, edgeLocation)

	          val result = Http(target)
		    .postData(doc)
		    .header("Content-Type", "application/json")
		    .header("Charset", "UTF-8")
		    .option(HttpOptions.readTimeout(10000)).asString

	          System.out.println (doc)
	          
	        case _ =>
	      }

	    case _ =>

	  }

        }

        bs.close

      case None =>
      case _ =>
    }

    result
  }

  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

}
