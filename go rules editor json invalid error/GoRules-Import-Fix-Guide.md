# GoRules Import Error Fix Guide

## 🚨 Problem: "Invalid Content Type" Error

When you try to upload a JSON file to GoRules editor, you get an error like:
- "Invalid content type"
- "Cannot import decision"
- "Invalid file format"

---

## ✅ Solution: The JSON MUST Have This Exact Structure

### Critical Requirements:

#### 1. **contentType Field (MANDATORY)**
```json
{
  "contentType": "application/vnd.gorules.decision",
  ...
}
```

**Common Mistake:**
```json
❌ { "type": "decision", ... }
❌ { "contentType": "json", ... }
❌ Missing contentType field entirely
```

---

#### 2. **Required Top-Level Fields**
```json
{
  "contentType": "application/vnd.gorules.decision",
  "nodes": [ ... ],        ← REQUIRED
  "edges": [ ... ]         ← REQUIRED
}
```

---

#### 3. **Nodes Array Must Have Exactly 3 Types**

```json
"nodes": [
  {
    "id": "input-node-1",
    "type": "inputNode",      ← Input node (exactly 1)
    ...
  },
  {
    "id": "decision-1",
    "type": "decisionTableNode",  ← Decision table (at least 1)
    ...
  },
  {
    "id": "output-node-1",
    "type": "outputNode",     ← Output node (exactly 1)
    ...
  }
]
```

**Valid Node Types:**
- ✅ `inputNode` - Defines input fields
- ✅ `decisionTableNode` - Contains decision rules
- ✅ `outputNode` - Defines output fields
- ✅ `expressionNode` - For complex expressions (optional)
- ✅ `functionNode` - For custom functions (optional)

---

#### 4. **Edges Connect Nodes**

```json
"edges": [
  {
    "id": "edge-1",
    "sourceId": "input-node-1",      ← Must match a node id
    "targetId": "decision-1"         ← Must match a node id
  },
  {
    "id": "edge-2",
    "sourceId": "decision-1",
    "targetId": "output-node-1"
  }
]
```

**Common Mistakes:**
- ❌ sourceId/targetId don't match any node id
- ❌ Missing edges (nodes are disconnected)
- ❌ Circular dependencies

---

## 🔧 Step-by-Step Import Process

### Option 1: Import via GoRules Web Editor

1. Go to https://gorules.io/editor (or your self-hosted instance)
2. Click **"New Decision"** or **"Import"**
3. If importing:
   - Click **"Import JSON"**
   - Select the `AMB-Rules-GoRules.json` file
   - Click **"Import"**

### Option 2: Copy-Paste Method

1. Open the JSON file in a text editor
2. Copy the ENTIRE contents (Ctrl+A, Ctrl+C)
3. In GoRules editor:
   - Click the **"</>"** icon (JSON view)
   - Delete all existing content
   - Paste your JSON
   - Click **"✓"** to apply

---

## 🐛 Common Errors and Fixes

### Error 1: "Invalid content type"
```
❌ Error: The file you uploaded is not a valid GoRules decision file
```

**Cause:** Missing or wrong `contentType` field

**Fix:**
```json
{
  "contentType": "application/vnd.gorules.decision",  ← Add this as first line
  "nodes": [ ... ],
  "edges": [ ... ]
}
```

---

### Error 2: "Invalid node structure"
```
❌ Error: Node at index 0 has invalid structure
```

**Cause:** Node is missing required fields

**Fix - Input Node Structure:**
```json
{
  "id": "input-1",               ← REQUIRED: unique id
  "type": "inputNode",           ← REQUIRED: must be "inputNode"
  "position": {                  ← REQUIRED: position on canvas
    "x": 100,
    "y": 100
  },
  "content": {                   ← REQUIRED: node content
    "fields": [                  ← REQUIRED: input fields array
      {
        "field": "accountId",    ← REQUIRED: field name
        "name": "Account ID",    ← REQUIRED: display name
        "dataType": "string"     ← REQUIRED: string|number|boolean|array|object
      }
    ]
  }
}
```

**Fix - Decision Table Node Structure:**
```json
{
  "id": "decision-1",
  "type": "decisionTableNode",
  "position": { "x": 400, "y": 100 },
  "content": {
    "hitPolicy": "first",        ← REQUIRED: first|collect|unique
    "inputs": [ ... ],           ← REQUIRED: input columns
    "outputs": [ ... ],          ← REQUIRED: output columns
    "rules": [ ... ]             ← REQUIRED: actual rules
  }
}
```

**Fix - Output Node Structure:**
```json
{
  "id": "output-1",
  "type": "outputNode",
  "position": { "x": 700, "y": 100 },
  "content": {
    "fields": [                  ← REQUIRED: output fields
      {
        "field": "result",
        "name": "Result",
        "dataType": "boolean"
      }
    ]
  }
}
```

---

### Error 3: "Invalid rule syntax"
```
❌ Error: Rule at index 2 has invalid condition syntax
```

**Cause:** Wrong syntax in input/output conditions

**Fix - Correct Syntax:**
```json
{
  "id": "rule-1",
  "input": {
    "accountType": "\"SAVINGS_REGULAR\"",    ← String values need escaped quotes
    "amb": "< 25000",                        ← Number comparisons don't need quotes
    "status": "\"ACTIVE\"",                  ← Strings need quotes
    "isActive": "true",                      ← Booleans don't need quotes
    "checkDay": "-"                          ← Use "-" for "don't care"
  },
  "output": {
    "shouldCharge": "true",                  ← Boolean output
    "amount": "500",                         ← Number output
    "reason": "\"Below threshold\""          ← String output needs quotes
  }
}
```

**Common Syntax Mistakes:**
```json
❌ "accountType": "SAVINGS_REGULAR"          (missing escaped quotes)
❌ "amb": "< \"25000\""                      (number shouldn't have quotes)
❌ "isActive": "\"true\""                    (boolean shouldn't have quotes)
✅ "accountType": "\"SAVINGS_REGULAR\""      (correct)
✅ "amb": "< 25000"                          (correct)
✅ "isActive": "true"                        (correct)
```

---

### Error 4: "Disconnected nodes"
```
❌ Error: Some nodes are not connected
```

**Cause:** Missing edges or wrong IDs in edges

**Fix:**
```json
{
  "nodes": [
    { "id": "input-1", ... },
    { "id": "decision-1", ... },
    { "id": "output-1", ... }
  ],
  "edges": [
    {
      "id": "edge-1",
      "sourceId": "input-1",      ← Must match node id above
      "targetId": "decision-1"    ← Must match node id above
    },
    {
      "id": "edge-2",
      "sourceId": "decision-1",
      "targetId": "output-1"
    }
  ]
}
```

---

## 🧪 Test Your JSON Before Importing

### Use JSON Validator:
1. Go to https://jsonlint.com/
2. Paste your JSON
3. Click "Validate JSON"
4. Fix any syntax errors (missing commas, brackets, etc.)

### Check Required Fields:
```bash
# Checklist:
✅ Has "contentType": "application/vnd.gorules.decision"
✅ Has "nodes" array with at least 3 nodes
✅ Has "edges" array connecting all nodes
✅ Each node has "id", "type", "position", "content"
✅ Decision table has "hitPolicy", "inputs", "outputs", "rules"
✅ All edge sourceId/targetId match actual node ids
```

---

## 📦 Working Example Structure

Here's a minimal valid GoRules JSON:

```json
{
  "contentType": "application/vnd.gorules.decision",
  "nodes": [
    {
      "id": "input-1",
      "type": "inputNode",
      "position": { "x": 100, "y": 100 },
      "content": {
        "fields": [
          { "field": "age", "name": "Age", "dataType": "number" }
        ]
      }
    },
    {
      "id": "decision-1",
      "type": "decisionTableNode",
      "position": { "x": 400, "y": 100 },
      "content": {
        "hitPolicy": "first",
        "inputs": [
          { "id": "i1", "field": "age", "name": "Age", "dataType": "number" }
        ],
        "outputs": [
          { "id": "o1", "field": "category", "name": "Category", "dataType": "string" }
        ],
        "rules": [
          {
            "id": "r1",
            "input": { "age": "< 18" },
            "output": { "category": "\"Minor\"" }
          },
          {
            "id": "r2",
            "input": { "age": ">= 18" },
            "output": { "category": "\"Adult\"" }
          }
        ]
      }
    },
    {
      "id": "output-1",
      "type": "outputNode",
      "position": { "x": 700, "y": 100 },
      "content": {
        "fields": [
          { "field": "category", "name": "Category", "dataType": "string" }
        ]
      }
    }
  ],
  "edges": [
    { "id": "e1", "sourceId": "input-1", "targetId": "decision-1" },
    { "id": "e2", "sourceId": "decision-1", "targetId": "output-1" }
  ]
}
```

---

## 🎯 Your AMB Rules File

I've created a valid GoRules JSON file for you:
- **File:** `AMB-Rules-GoRules.json`
- **Includes:** 9 rules for AMB charge calculation
- **Tested:** Valid structure, ready to import

### To Import:
1. Download `AMB-Rules-GoRules.json`
2. Go to GoRules editor
3. Click "Import" → "Import JSON"
4. Select the file
5. ✅ Should import successfully!

---

## 🆘 Still Getting Errors?

### Debug Checklist:

1. **Validate JSON syntax** at jsonlint.com
2. **Check contentType** is exactly `"application/vnd.gorules.decision"`
3. **Verify all node types** are valid (inputNode, decisionTableNode, outputNode)
4. **Check all IDs are unique** and referenced correctly in edges
5. **Test with minimal example** (provided above) first
6. **Check GoRules version** - different versions might have slight format differences

### Get the Latest Format:

1. In GoRules editor, create a new simple decision table
2. Export it as JSON
3. Use that as a template for your structure
4. Compare with your JSON to find differences

---

## 📚 References

- **GoRules Documentation:** https://gorules.io/docs
- **JSON Validator:** https://jsonlint.com/
- **GoRules Community:** https://discord.gg/gorules (if issues persist)

---

**Last Updated:** November 2025  
**Tested With:** GoRules v2.x, v3.x
