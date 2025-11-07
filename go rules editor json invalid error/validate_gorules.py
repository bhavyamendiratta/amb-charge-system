#!/usr/bin/env python3
"""
GoRules JSON Validator
Validates a JSON file before importing to GoRules editor
"""

import json
import sys
from typing import Dict, List, Any

def validate_gorules_json(file_path: str) -> tuple[bool, List[str]]:
    """
    Validate a GoRules JSON file.
    
    Returns:
        (is_valid, error_messages)
    """
    errors = []
    
    try:
        # Load JSON file
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except json.JSONDecodeError as e:
        return False, [f"❌ Invalid JSON syntax: {e}"]
    except FileNotFoundError:
        return False, [f"❌ File not found: {file_path}"]
    except Exception as e:
        return False, [f"❌ Error reading file: {e}"]
    
    # Check contentType
    if 'contentType' not in data:
        errors.append("❌ Missing required field: 'contentType'")
    elif data['contentType'] != 'application/vnd.gorules.decision':
        errors.append(f"❌ Invalid contentType: '{data['contentType']}' (should be 'application/vnd.gorules.decision')")
    else:
        print("✅ Valid contentType")
    
    # Check nodes
    if 'nodes' not in data:
        errors.append("❌ Missing required field: 'nodes'")
    elif not isinstance(data['nodes'], list):
        errors.append("❌ 'nodes' must be an array")
    else:
        nodes = data['nodes']
        print(f"✅ Found {len(nodes)} nodes")
        
        # Validate each node
        node_ids = set()
        has_input = False
        has_decision = False
        has_output = False
        
        for i, node in enumerate(nodes):
            # Check required fields
            if 'id' not in node:
                errors.append(f"❌ Node at index {i} missing 'id' field")
            else:
                node_id = node['id']
                if node_id in node_ids:
                    errors.append(f"❌ Duplicate node id: '{node_id}'")
                node_ids.add(node_id)
            
            if 'type' not in node:
                errors.append(f"❌ Node '{node.get('id', i)}' missing 'type' field")
            else:
                node_type = node['type']
                valid_types = ['inputNode', 'decisionTableNode', 'outputNode', 'expressionNode', 'functionNode']
                if node_type not in valid_types:
                    errors.append(f"❌ Node '{node.get('id')}' has invalid type: '{node_type}'")
                
                # Track node types
                if node_type == 'inputNode':
                    has_input = True
                elif node_type == 'decisionTableNode':
                    has_decision = True
                elif node_type == 'outputNode':
                    has_output = True
            
            if 'position' not in node:
                errors.append(f"❌ Node '{node.get('id', i)}' missing 'position' field")
            elif not isinstance(node['position'], dict) or 'x' not in node['position'] or 'y' not in node['position']:
                errors.append(f"❌ Node '{node.get('id')}' position must have 'x' and 'y' fields")
            
            if 'content' not in node:
                errors.append(f"❌ Node '{node.get('id', i)}' missing 'content' field")
            else:
                # Validate content based on node type
                content = node['content']
                if node['type'] in ['inputNode', 'outputNode']:
                    if 'fields' not in content:
                        errors.append(f"❌ Node '{node.get('id')}' content missing 'fields' array")
                    elif not isinstance(content['fields'], list):
                        errors.append(f"❌ Node '{node.get('id')}' content.fields must be an array")
                    else:
                        for field in content['fields']:
                            if 'field' not in field:
                                errors.append(f"❌ Field in node '{node.get('id')}' missing 'field' name")
                            if 'dataType' not in field:
                                errors.append(f"❌ Field '{field.get('field', '?')}' in node '{node.get('id')}' missing 'dataType'")
                
                elif node['type'] == 'decisionTableNode':
                    required_fields = ['hitPolicy', 'inputs', 'outputs', 'rules']
                    for field in required_fields:
                        if field not in content:
                            errors.append(f"❌ Decision table '{node.get('id')}' missing '{field}' field")
                    
                    if 'hitPolicy' in content and content['hitPolicy'] not in ['first', 'collect', 'unique']:
                        errors.append(f"❌ Decision table '{node.get('id')}' has invalid hitPolicy: '{content['hitPolicy']}'")
                    
                    if 'rules' in content:
                        print(f"   ├─ Decision table '{node.get('id')}' has {len(content['rules'])} rules")
        
        # Check for required node types
        if not has_input:
            errors.append("⚠️  Warning: No inputNode found (recommended to have at least one)")
        else:
            print("✅ Found inputNode")
        
        if not has_decision:
            errors.append("⚠️  Warning: No decisionTableNode found")
        else:
            print("✅ Found decisionTableNode")
        
        if not has_output:
            errors.append("⚠️  Warning: No outputNode found (recommended to have at least one)")
        else:
            print("✅ Found outputNode")
    
    # Check edges
    if 'edges' not in data:
        errors.append("❌ Missing required field: 'edges'")
    elif not isinstance(data['edges'], list):
        errors.append("❌ 'edges' must be an array")
    else:
        edges = data['edges']
        print(f"✅ Found {len(edges)} edges")
        
        for i, edge in enumerate(edges):
            if 'id' not in edge:
                errors.append(f"❌ Edge at index {i} missing 'id' field")
            
            if 'sourceId' not in edge:
                errors.append(f"❌ Edge '{edge.get('id', i)}' missing 'sourceId'")
            elif edge['sourceId'] not in node_ids:
                errors.append(f"❌ Edge '{edge.get('id')}' sourceId '{edge['sourceId']}' does not match any node id")
            
            if 'targetId' not in edge:
                errors.append(f"❌ Edge '{edge.get('id', i)}' missing 'targetId'")
            elif edge['targetId'] not in node_ids:
                errors.append(f"❌ Edge '{edge.get('id')}' targetId '{edge['targetId']}' does not match any node id")
    
    return len(errors) == 0, errors


def main():
    """Main entry point."""
    if len(sys.argv) < 2:
        print("Usage: python3 validate_gorules.py <json_file>")
        print("\nExample:")
        print("  python3 validate_gorules.py AMB-Rules-GoRules.json")
        sys.exit(1)
    
    file_path = sys.argv[1]
    
    print(f"\n🔍 Validating GoRules JSON: {file_path}")
    print("=" * 60)
    
    is_valid, errors = validate_gorules_json(file_path)
    
    print("\n" + "=" * 60)
    
    if is_valid:
        print("\n✅ ✅ ✅  VALIDATION PASSED  ✅ ✅ ✅")
        print("\n🎉 Your JSON file is valid and ready to import to GoRules!")
        print("\nNext steps:")
        print("1. Go to GoRules editor (https://gorules.io/editor)")
        print("2. Click 'Import' → 'Import JSON'")
        print(f"3. Select: {file_path}")
        print("4. Click 'Import'")
        return 0
    else:
        print("\n❌ ❌ ❌  VALIDATION FAILED  ❌ ❌ ❌")
        print(f"\n{len(errors)} error(s) found:\n")
        for error in errors:
            print(f"  {error}")
        
        print("\n\n🔧 How to fix:")
        print("1. Address the errors listed above")
        print("2. Run this validator again")
        print("3. Repeat until validation passes")
        print("\nSee: GoRules-Import-Fix-Guide.md for detailed help")
        return 1


if __name__ == '__main__':
    sys.exit(main())
