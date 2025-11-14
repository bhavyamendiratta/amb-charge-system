package com.example.gorules.controller;

import com.example.gorules.model.CreateRuleRequest;
import com.example.gorules.model.RuleResponse;
import com.example.gorules.model.UpdateRuleRequest;
import com.example.gorules.service.RuleManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules/management")
@RequiredArgsConstructor
public class RuleManagementController {

    private final RuleManagementService ruleManagementService;
    private final ObjectMapper objectMapper;

    /**
     * Create a new rule with JSON body
     */
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody CreateRuleRequest request) {
        try {
            RuleResponse response = ruleManagementService.createRule(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create a new rule by uploading a JSON file for ruleContent
     * Use form-data with:
     * - ruleFile: file (the JSON rule file)
     * - ruleId: text
     * - ruleName: text
     * - description: text (optional)
     * - createdBy: text (optional)
     * - status: text (optional, default: ACTIVE)
     * - version: text (optional, default: 1.0)
     */
    @PostMapping("/upload")
    public ResponseEntity<RuleResponse> createRuleWithFile(
            @RequestParam("ruleFile") MultipartFile ruleFile,
            @RequestParam("ruleId") String ruleId,
            @RequestParam("ruleName") String ruleName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "createdBy", required = false, defaultValue = "system") String createdBy,
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(value = "version", required = false, defaultValue = "1.0") String version) {
        try {
            // Read the file content
            String ruleContent = new String(ruleFile.getBytes());

            // Validate it's valid JSON
            objectMapper.readTree(ruleContent);

            // Create the request
            CreateRuleRequest request = CreateRuleRequest.builder()
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .description(description)
                    .ruleContent(ruleContent)
                    .createdBy(createdBy)
                    .status(status)
                    .version(version)
                    .build();

            RuleResponse response = ruleManagementService.createRule(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Update a rule by uploading a new JSON file for ruleContent
     */
    @PutMapping("/{ruleId}/upload")
    public ResponseEntity<RuleResponse> updateRuleWithFile(
            @PathVariable String ruleId,
            @RequestParam("ruleFile") MultipartFile ruleFile,
            @RequestParam(value = "ruleName", required = false) String ruleName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "version", required = false) String version) {
        try {
            // Read the file content
            String ruleContent = new String(ruleFile.getBytes());

            // Validate it's valid JSON
            objectMapper.readTree(ruleContent);

            // Create the update request
            UpdateRuleRequest request = UpdateRuleRequest.builder()
                    .ruleName(ruleName)
                    .description(description)
                    .ruleContent(ruleContent)
                    .status(status)
                    .version(version)
                    .build();

            RuleResponse response = ruleManagementService.updateRule(ruleId, request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all rules
     */
    @GetMapping
    public ResponseEntity<List<RuleResponse>> getAllRules() {
        List<RuleResponse> rules = ruleManagementService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get rule by ID
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<RuleResponse> getRuleById(@PathVariable String ruleId) {
        try {
            RuleResponse response = ruleManagementService.getRuleById(ruleId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get rules by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RuleResponse>> getRulesByStatus(@PathVariable String status) {
        List<RuleResponse> rules = ruleManagementService.getRulesByStatus(status);
        return ResponseEntity.ok(rules);
    }

    /**
     * Update a rule
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<RuleResponse> updateRule(
            @PathVariable String ruleId,
            @RequestBody UpdateRuleRequest request) {
        try {
            RuleResponse response = ruleManagementService.updateRule(ruleId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a rule
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable String ruleId) {
        try {
            ruleManagementService.deleteRule(ruleId);
            return ResponseEntity.ok(Map.of(
                    "message", "Rule deleted successfully",
                    "ruleId", ruleId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get rule content only (JSON)
     */
    @GetMapping("/{ruleId}/content")
    public ResponseEntity<String> getRuleContent(@PathVariable String ruleId) {
        try {
            String content = ruleManagementService.getRuleContent(ruleId);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(content);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download rule content as JSON file
     */
    @GetMapping("/{ruleId}/download")
    public ResponseEntity<String> downloadRuleContent(@PathVariable String ruleId) {
        try {
            RuleResponse rule = ruleManagementService.getRuleById(ruleId);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .header("Content-Disposition", "attachment; filename=\"" + ruleId + ".json\"")
                    .body(rule.getRuleContent());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}