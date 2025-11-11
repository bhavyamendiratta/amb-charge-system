curl --location 'http://localhost:8080/api/rules/execute' \
--form 'rulesFile=@"/Users/bhavya/Desktop/gorules-engine/src/main/resources/rules/sample-discount-rules.json"' \
--form 'data="{\"income\":5500,\"tier\":\"gold\"}"'
