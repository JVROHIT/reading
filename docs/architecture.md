# Reading & Learning Companion - Architecture

## 1. Goal

Build a simple web-based backend service that:
- Accepts a user **instruction** and **text content**
- Uses an LLM-based **orchestrator** to decide whether the user wants a **SUMMARY** or a **QUIZ**
- Calls the appropriate **agent service** (`SummaryAgentService` or `QuizAgentService`)
- Returns a structured response to the frontend

---

## 2. High-Level Flow

1. `LearningController` exposes a REST endpoint:
   - `POST /api/learning`
   - Request body: `LearningRequest` (instruction + text)

2. `LearningService`:
   - Validates the request  
   - Request validation includes:
     - Both `instruction` and `text` are present
     - Valid `instruction` values (for now): **SUMMARY** and **QUIZ**
     - Valid content checks:
       - `text` not being empty
       - Basic SQL injection guard (no obvious SQL keywords / patterns)
       - Character limit of **50k characters**
   - Delegates to `OrchestratorService`

3. `OrchestratorService`:
   - Calls `QuizSummaryDecisionService` (via `OrchestratorAgentService`) to decide action: `SUMMARY` or `QUIZ`
   - If `SUMMARY`: calls `SummaryAgentService`
   - If `QUIZ`: calls `QuizAgentService`
   - Builds and returns `LearningResponse`

4. `OrchestratorAgentService`:
   - An interface with the method:  
     - `MasterDecisionResponse generateDecision(LearningRequest request)`
   - `MasterDecisionResponse` contains:
     - `ActionType actionToTake` (SUMMARY or QUIZ)
     - `String explanation`

5. `ChildAgentService`:
   - An interface with the method:  
     - `AgentResponse generateResponse(LearningRequest request)`
   - `AgentResponse` contains:
     - `ActionType action` (SUMMARY or QUIZ)
     - `String content` — raw LLM response (summary text or quiz JSON/string)
     - `String explanation` — why this response was generated / any notes

6. `QuizSummaryDecisionService`:
   - Uses an LLM with an **orchestrator prompt**
   - Implements `OrchestratorAgentService`
   - Input: `LearningRequest` (primarily `instruction`, optionally content metadata)
   - LLM output is parsed into `DecisionLlmResponse` which has fields:
     - `String decision` — "SUMMARY" or "QUIZ"
     - `String explanation` — natural language explanation
   - `DecisionLlmResponse` is then mapped to:
     - `MasterDecisionResponse.actionToTake` = `ActionType.SUMMARY` or `ActionType.QUIZ`
     - `MasterDecisionResponse.explanation` = explanation from LLM

7. `SummaryAgentService`:
   - Uses an LLM with a **summary prompt**
   - Implements `ChildAgentService`
   - Input: full text + user instruction
   - Output: `AgentResponse` with:
     - `action` = `ActionType.SUMMARY`
     - `content` = summary text (Markdown allowed)
     - `explanation` = optional explanation / meta info from LLM (can be empty in v1)

8. `QuizAgentService`:
   - Uses an LLM with a **quiz-generation prompt**
   - Implements `ChildAgentService`
   - Input: full text + user instruction
   - Output: `AgentResponse` with:
     - `action` = `ActionType.QUIZ`
     - `content` = quiz payload (JSON string or raw text which is then parsed into `QuizQuestionDto` list)
     - `explanation` = optional explanation / notes from LLM

9. `LlmClientService`:
   - Wraps calls to the actual LLM API (e.g., OpenAI)
   - All LLM-related services use this client
   - Responsibility:
     - Build chat payload (system + user messages)
     - Call external API
     - Return raw string response

10. `LlmClientConfig`:
   - Configures API key, base URL, and model name for the LLM client
   - Exposes a configured `LlmClient` bean

---

## 3. Package Structure

- `controller`
  - `LearningController`

- `dto`
  - `LearningRequest`
  - `LearningResponse`
  - `MasterDecisionResponse`
  - `AgentResponse`
  - `DecisionLlmResponse`
  - `QuizQuestionDto`
  - `ActionType`

- `service`
  - `LearningService`
  - `OrchestratorService`
  - `parentAgent`
    - `OrchestratorAgentService`
    - `QuizSummaryDecisionService`
  - `childAgent`
    - `ChildAgentService`
    - `SummaryAgentService`
    - `QuizAgentService`
  - `llm`
    - `LlmClientService`

- `config`
  - `LlmClientConfig`

---

## 4. DTO Definitions (Conceptual)

### 4.1 LearningRequest

Fields:
- `String instruction` — natural language instruction from user  
  (e.g. "summarise this for revision", "create a quiz", "test me on this chapter")
- `String text` — content to summarise or quiz on

### 4.2 LearningResponse

Fields:
- `ActionType action` — SUMMARY or QUIZ (final decision taken)
- `String summary` — filled only if `action == SUMMARY`
- `List<QuizQuestionDto> quiz` — filled only if `action == QUIZ`
- `String explanation` — optional, may contain:
  - why the orchestrator chose this action
  - any meta info from agents

### 4.3 ActionType (enum)

Values:
- `SUMMARY`
- `QUIZ`

### 4.4 MasterDecisionResponse

Fields:
- `ActionType actionToTake` — SUMMARY or QUIZ
- `String explanation` — explanation returned by the orchestrator LLM

### 4.5 DecisionLlmResponse

Represents the **raw structured LLM output** from the orchestrator prompt before mapping to `MasterDecisionResponse`.

Fields:
- `String decision` — "SUMMARY" or "QUIZ"
- `String explanation` — natural language explanation from model

### 4.6 AgentResponse

Fields:
- `ActionType action` — SUMMARY or QUIZ (corresponding to the agent)
- `String content` — raw LLM response (summary text or quiz JSON/string)
- `String explanation` — optional explanation, can include:
  - how the summary was produced / style
  - how the quiz is structured

### 4.7 QuizQuestionDto

Represents a single quiz question after parsing `AgentResponse.content` when `action == QUIZ`.

Fields:
- `String type` — e.g. `"MCQ"`, `"SHORT_ANSWER"`, `"TRUE_FALSE"`
- `String question`
- `List<String> options` — empty for non-MCQ types
- `String answer` — correct answer or expected key points

---

## 5. LLM Responsibilities

### 5.1 Orchestrator Prompt (for QuizSummaryDecisionService)

**Role:**  
Decide whether the user's request is best served by a **SUMMARY** or a **QUIZ**.

**Input:**
- User instruction (mandatory)
- Optionally: length of text, short snippet of content

**Behavior:**
- If user clearly wants:
  - explanation / TL;DR / overview → choose `"SUMMARY"`
  - questions / test / exam / practice → choose `"QUIZ"`
- If ambiguous:
  - Default to `"SUMMARY"`

**Output (JSON):**
```
{
  "decision": "SUMMARY",
  "explanation": "User asked for a short overview and did not mention testing or questions."
}
```
### 5.2 Summary Agent Prompt
**Role:**  
expert summariser.
**Input:**
- User instruction
- Full text
**Behavior:**
-Produce a concise, accurate summary
-Preserve key concepts, definitions, and relationships
-Obey any user-specified style (e.g. "for revision notes", "for a 10-year-old")
**Output:**
Markdown summary text (string), stored in AgentResponse.content.


### 5.3 Quiz Agent Prompt
**Role:** 
quiz generator.
**Input:**
-User instruction
-Full text
**Behavior:**
Generate quiz questions that test understanding and recall
Use a mix of question types where appropriate
Base every question strictly on the given text
**Output (JSON):**
```
{
  "questions": [
    {
      "type": "MCQ",
      "question": "What is the primary purpose of X?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "answer": "Option B"
    },
    {
      "type": "SHORT_ANSWER",
      "question": "Explain the relationship between Y and Z.",
      "options": [],
      "answer": "User should mention that Y depends on Z for..."
    }
  ]
}
```

This JSON is placed in AgentResponse.content as a string and then deserialized into List<QuizQuestionDto> in the application layer.

---

### 6. Non-Functional
Use constructor injection for all services.
Add basic logging around LLM calls (request ID, action type, latency).
For now:
No persistence (no database).
No authentication/authorization.
Keep prompts in src/main/resources/prompts/ for easier iteration:
- orchestrator_prompt.md
- summary_agent_prompt.md
- quiz_agent_prompt.md