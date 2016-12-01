# cloudfront-report

Scala code as AWS Lambda which can be triggered automaticlly when there is a new CloudFront log coming to your S3 bucket. 
Logs will be parsed and index into Amazon Elasticsearch Service and display as a performance report  on Kibana.

![alt tag](https://c8.staticflickr.com/6/5820/30383149743_dae066abdb_k.jpg)

## Deploy AWS Lambda
```javascript
// build
$ git clone https://github.com/jameswu0629/cloudfront-report.git
$ cd cloudfront-report
$ ./bin/activator assembly

// upload your package to S3
$ aws s3 cp cloudfront-report-x.x-SNAPSHOT.jar s3://[BUCKET_NAME]/source/

// 999999999999 is your account id
$ aws lambda create-function \
--function-name indexLogToES \
--code '{"S3Bucket": "[BUCKET_NAME]","S3Key": "source/cloudfront-report-x.x-SNAPSHOT.jar"}' \
--role arn:aws:iam::999999999999:role/lambda_s3_exec_role \
--handler cn.amazonaws.CloudfrontReport::indexLogToES \
--runtime java8 \
--timeout 300 \
--memory-size 1024
```

## Setup trigger
![alt tag](https://c6.staticflickr.com/6/5454/30383461733_5ae3b7e2f7_b.jpg)

## Elasticsearch mapping
```javascript
{
	"template": "logstash-*",
	"mappings": {
		"type1": {
			"properties": {
				"created_at": {
					"type": "date",
					"format": "yyyy-MM-dd HH:mm:ss"
				},
				"country": {
					"type": "string",
					"index": "not_analyzed"
				},
				"location": {
					"type": "geo_point"
				},
				"download_speed": {
					"type": "double",
				},
				"edge_location": {
					"type": "string",
					"index": "not_analyzed"
				}
			}
		}
	}
}
```

## Kibana
### Settings
![alt tag](https://c1.staticflickr.com/6/5628/31201332072_fc98b234bd_b.jpg)


## TODO
- Use Elasticsearch Bulk API to batch process documents.
