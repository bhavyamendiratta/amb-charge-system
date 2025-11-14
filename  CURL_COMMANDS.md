# GoRules Engine - File Upload API Examples

## Create Rule with File Upload

### Basic Usage
```bash
curl -X POST http://localhost:8080/api/rules/management/upload \
  -F "ruleFile=@amb-defaulter-rule.json" \
  -F "ruleId=amb-defaulter-detection-v1" \
  -F "ruleName=AMB Defaulter Detection Rules"
```

### With All Parameters
```bash
curl -X POST http://localhost:8080/api/rules/management/upload \
  -F "ruleFile=@amb-defaulter-rule.json" \
  -F "ruleId=amb-defaulter-detection-v1" \
  -F "ruleName=AMB Defaulter Detection Rules" \
  -F "description=Rules for detecting probable and actual AMB defaulters with charge calculation" \
  -F "createdBy=admin" \
  -F "status=ACTIVE" \
  -F "version=1.0"
```

### Another Example - Loan Eligibility
```bash
curl -X POST http://localhost:8080/api/rules/management/upload \
  -F "ruleFile=@loan-eligibility.json" \
  -F "ruleId=loan-eligibility-v1" \
  -F "ruleName=Loan Eligibility Rules" \
  -F "description=Comprehensive loan eligibility checking" \
  -F "createdBy=system" \
  -F "status=ACTIVE" \
  -F "version=1.0"
```

## Update Rule with File Upload

### Update Rule Content
```bash
curl -X PUT http://localhost:8080/api/rules/management/amb-defaulter-detection-v1/upload \
  -F "ruleFile=@amb-defaulter-rule-updated.json"
```

### Update Rule Content and Metadata
```bash
curl -X PUT http://localhost:8080/api/rules/management/amb-defaulter-detection-v1/upload \
  -F "ruleFile=@amb-defaulter-rule-v2.json" \
  -F "ruleName=AMB Defaulter Detection Rules V2" \
  -F "description=Updated rules with new logic" \
  -F "status=ACTIVE" \
  -F "version=2.0"
```

## Download Rule as File
```bash
# Download rule content
curl -X GET http://localhost:8080/api/rules/management/amb-defaulter-detection-v1/download \
  -o downloaded-rule.json
```

## Execute Rule (Existing Endpoints)

### Execute by Rule ID
```bash
# Test Case 1: Probable Defaulter on Day 25 (New)
curl -X POST http://localhost:8080/api/rules/execute/amb-defaulter-detection-v1 \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "checkDay": 25,
      "ambDay1To25": 8000,
      "minBalance": 10000,
      "wasActualDefaulterLastMonth": false
    }
  }'

# Expected Output:
# {
#   "success": true,
#   "result": {
#     "probableDefaulterAction": "MARK_PROBABLE_DEFAULTER",
#     "sendSMS": true,
#     "probableDefaulterReason": "New probable defaulter - SMS sent"
#   },
#   "executionTimeMs": 45
# }
```
```bash
# Test Case 2: Probable Defaulter on Day 25 (Was Actual Last Month)
curl -X POST http://localhost:8080/api/rules/execute/amb-defaulter-detection-v1 \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "checkDay": 25,
      "ambDay1To25": 8000,
      "minBalance": 10000,
      "wasActualDefaulterLastMonth": true
    }
  }'

# Expected Output:
# {
#   "success": true,
#   "result": {
#     "probableDefaulterAction": "MARK_PROBABLE_DEFAULTER",
#     "sendSMS": false,
#     "probableDefaulterReason": "Was actual defaulter last month - NO SMS"
#   },
#   "executionTimeMs": 42
# }
```
```bash
# Test Case 3: Actual Defaulter on Day 3
curl -X POST http://localhost:8080/api/rules/execute/amb-defaulter-detection-v1 \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "checkDay": 3,
      "ambDay1To30": 9000,
      "minBalance": 10000,
      "wasProbableDefaulterLastMonth": true
    }
  }'

# Expected Output:
# {
#   "success": true,
#   "result": {
#     "actualDefaulterAction": "MARK_ACTUAL_DEFAULTER",
#     "actualDefaulterStatus": "CONFIRMED_DEFAULTER"
#   },
#   "executionTimeMs": 38
# }
```
```bash
# Test Case 4: Charge Calculation for 2 Consecutive Months
curl -X POST http://localhost:8080/api/rules/execute/amb-defaulter-detection-v1 \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "checkDay": 3,
      "actualDefaulterMonth2": true,
      "actualDefaulterMonth1": true,
      "shortfallMonth1": 2000,
      "shortfallMonth2": 1500
    }
  }'

# Expected Output:
# {
#   "success": true,
#   "result": {
#     "chargeAction": "APPLY_CHARGE",
#     "chargeType": "TWO_MONTH_CONSECUTIVE"
#   },
#   "executionTimeMs": 40
# }
```

## Other Management APIs

### List All Rules
```bash
curl -X GET http://localhost:8080/api/rules/management
```

### Get Rule by ID
```bash
curl -X GET http://localhost:8080/api/rules/management/amb-defaulter-detection-v1
```

### Get Rules by Status
```bash
curl -X GET http://localhost:8080/api/rules/management/status/ACTIVE
```

### Delete Rule
```bash
curl -X DELETE http://localhost:8080/api/rules/management/amb-defaulter-detection-v1
```

### Get Rule Content (JSON only)
```bash
curl -X GET http://localhost:8080/api/rules/management/amb-defaulter-detection-v1/content
```