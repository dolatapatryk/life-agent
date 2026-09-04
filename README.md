# Life Agent

A command-line personal AI assistant built with Kotlin and a local language model served through Ollama. It explores how an agent can plan a request, call tools, and use their results to respond to the user.

I am building this project for educational purposes, to learn about AI agents, tool calling, prompt design, and integrating local language models into an application.

The prompts in the code are intentionally written in Polish because I am testing how well a local model handles the Polish language. The assistant is also instructed to respond in Polish.

## Features

- Interactive conversations in the terminal, with conversation history kept during the session.
- A planning step before each request is executed.
- Tools for retrieving the current date and time, creating tasks, and listing saved tasks.
- Local task persistence in `tasks.json` in the working directory.
- Tool argument validation and tool error feedback to the model.
- An execution limit of 10 agent iterations per request.

## How it works

1. The user enters a request in the terminal.
2. `LlmPlanner` asks the model to produce a short execution plan.
3. `LifeAssistant` sends the request and plan to the model, together with the available tool definitions.
4. When the model requests a tool call, the assistant validates its arguments, executes the tool, and sends the result back to the model.
5. This loop continues until the model returns a text response or the iteration limit is reached.

## Technology

- Kotlin/JVM with a Java 23 toolchain
- Gradle with Kotlin DSL and the included Gradle Wrapper
- Ktor HTTP client
- Kotlinx Serialization for JSON
- Ollama for local model inference
- Logback for logging

## Getting started

You need JDK 23 and Ollama installed. The application currently uses `qwen3:1.7b` and connects to `http://localhost:11434/api/chat`.

1. Start Ollama if it is not already running:

   ```sh
   ollama serve
   ```

2. In another terminal, download the configured model:

   ```sh
   ollama pull qwen3:1.7b
   ```

3. From the repository root, start the assistant:

   ```sh
   ./gradlew run --console=plain
   ```

   On Windows, use `gradlew.bat run --console=plain`.

Enter a request at the `You:` prompt. You can ask for the current date or time, create a task with a due date, or list saved tasks. Type `exit` to quit.

The model name can be changed by passing a different value to `LocalLlmClient` in `Main.kt`. The Ollama endpoint is currently defined in `LocalLlmClient.kt`.

## Project structure

Source files live under `src/main/kotlin/com/patrykdolata/lifeagent/`:

```text
Main.kt             Application setup and terminal input loop
LifeAssistant.kt    Agent execution loop and conversation history
llm/                Model client interface, messages, and tool validation
llm/local/          Ollama HTTP client and API data classes
plan/               Planning interface and model-based planner
task/               Task model and file/in-memory repositories
tool/               Tool definitions and implementations
```

## Current limitations

This is an educational prototype. Model behavior and tool selection may vary, and the project is still evolving. Tasks currently support creation and listing; editing, completion, and deletion are not implemented. The task creation tool currently requires a due date, even though its tool schema marks that parameter as optional. Conversation history is kept in memory and is not restored after restarting the application.
