# GoRules JSON Quick Reference Card

## 🎯 The #1 Most Common Issue

### ❌ WRONG (causes "Invalid content type" error):
```json
{
  "nodes": [ ... ],
  "edges": [ ... ]
}
```

### ✅ CORRECT:
```json
{
  "contentType": "application/vnd.gorules.decision",
  "nodes": [ ... ],
  "edges": [ ... ]
}
```

**The first line MUST be the contentType!**

---

## 📋 Required JSON Structure

```json
{
  "contentType": "application/vnd.gorules.decision",
  "nodes": [
    { /* input node */ },
    { /* decision table node */ },
    { /* output node */ }
  ],
  "edges": [
    { /* edge connecting nodes */ }
  ]
}
```

---

## 🔧 String Values in Rules

### ❌ WRONG:
```json
"input": {
  "accountType": "SAVINGS_REGULAR"     ← Missing quotes around string value
}
```

### ✅ CORRECT:
```json
"input": {
  "accountType": "\"SAVINGS_REGULAR\""  ← String values need escaped quotes
}
```

---

## 🔢 Number and Boolean Values

### Numbers (no quotes needed):
```json
"input": {
  "amb": "< 25000",        ← Correct
  "age": ">= 18",          ← Correct
  "balance": "!= 0"        ← Correct
}
```

### Booleans (no quotes needed):
```json
"output": {
  "shouldCharge": "true",   ← Correct
  "isActive": "false"       ← Correct
}
```

---

## 🎲 "Don't Care" Condition

Use `"-"` when you don't care about a field's value:

```json
"input": {
  "accountType": "\"SAVINGS\"",
  "status": "-",              ← Matches any value
  "age": "-"                  ← Matches any value
}
```

---

## 🔗 Connecting Nodes with Edges

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
      "sourceId": "input-1",      ← Must match a node id
      "targetId": "decision-1"    ← Must match a node id
    },
    {
      "id": "edge-2",
      "sourceId": "decision-1",
      "targetId": "output-1"
    }
  ]
}
```

**Rule:** Every node should be connected!

---

## 📦 Complete Minimal Example

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
          { "id": "o1", "field": "result", "name": "Result", "dataType": "string" }
        ],
        "rules": [
          {
            "id": "r1",
            "input": { "age": "< 18" },
            "output": { "result": "\"Minor\"" }
          },
          {
            "id": "r2",
            "input": { "age": ">= 18" },
            "output": { "result": "\"Adult\"" }
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
          { "field": "result", "name": "Result", "dataType": "string" }
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

## 🧪 Validation Checklist

Before importing to GoRules, check:

- [ ] First line is `"contentType": "application/vnd.gorules.decision"`
- [ ] Has `"nodes"` array with at least 3 nodes
- [ ] Has `"edges"` array connecting nodes
- [ ] All node IDs are unique
- [ ] All edge sourceId/targetId match node IDs
- [ ] String values in rules have escaped quotes: `"\"VALUE\""`
- [ ] Number and boolean values DON'T have quotes
- [ ] JSON is valid (test at jsonlint.com)

---

## 🚀 Import Steps

1. **Validate your JSON first:**
   ```bash
   python3 validate_gorules.py your-file.json
   ```

2. **Import to GoRules:**
   - Go to https://gorules.io/editor
   - Click "Import" → "Import JSON"
   - Select your JSON file
   - Click "Import"

3. **If it fails:**
   - Check the error message
   - See "GoRules-Import-Fix-Guide.md"
   - Run validator again

---

## 📞 Quick Fixes

| Error | Fix |
|-------|-----|
| "Invalid content type" | Add `"contentType": "application/vnd.gorules.decision"` as first line |
| "Invalid node structure" | Check node has `id`, `type`, `position`, `content` |
| "Invalid rule syntax" | String values need `\"VALUE\"`, numbers/booleans don't |
| "Disconnected nodes" | Add edges connecting all nodes |
| "Duplicate ID" | Make sure all node/edge IDs are unique |

---

**Pro Tip:** Export a simple decision from GoRules as JSON and use it as a template!
