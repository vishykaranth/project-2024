# 🤖 AI Agent Build Guide - Step by Step

**Complete guide to building production-ready AI agents from scratch**

---

## 📋 Table of Contents

1. [Introduction to AI Agents](#introduction)
2. [Agent Architecture Fundamentals](#architecture)
3. [Step-by-Step Build Process](#build-process)
4. [Tools & Function Calling](#tools)
5. [Memory Systems](#memory)
6. [Agent Frameworks Comparison](#frameworks)
7. [Production Deployment](#deployment)
8. [Advanced Patterns](#advanced)
9. [Real-World Examples](#examples)

---

## Introduction to AI Agents

### What is an AI Agent?

An AI agent is a system that can:
- **Perceive** its environment (receive inputs)
- **Reason** about what to do (LLM decision-making)
- **Act** on decisions (execute tools/functions)
- **Learn** from outcomes (maintain memory/context)

### Agent vs. Simple LLM Call

**Simple LLM:**
```
User → LLM → Response
```

**AI Agent:**
```
User → Agent → [Reasoning Loop] → Response
              ↓        ↑
           [Tools] [Memory]
```

### Types of AI Agents

1. **ReAct Agent** - Reasoning + Acting in loops
2. **Planning Agent** - Creates multi-step plans
3. **Autonomous Agent** - Continuous operation
4. **Multi-Agent System** - Multiple agents collaborating
5. **Tool-Using Agent** - Focuses on external APIs

---

## Architecture Fundamentals

### Core Components

```
┌─────────────────────────────────────────┐
│           User Interface                │
└──────────────┬──────────────────────────┘
               ↓
┌──────────────────────────────────────────┐
│         Agent Orchestrator               │
│  - Reasoning Loop                        │
│  - Tool Selection                        │
│  - Memory Management                     │
└──┬───────────┬──────────────┬───────────┘
   │           │              │
   ↓           ↓              ↓
┌──────┐  ┌─────────┐  ┌──────────┐
│ LLM  │  │ Tools   │  │  Memory  │
│      │  │ - Web   │  │- Short   │
│      │  │ - Code  │  │- Long    │
│      │  │ - DB    │  │- Vector  │
└──────┘  └─────────┘  └──────────┘
```

### The ReAct Loop

**Re**asoning + **Act**ing pattern:

```
1. Thought: "I need to find current weather"
2. Action: Call weather_tool("San Francisco")
3. Observation: "72°F, sunny"
4. Thought: "I have the answer"
5. Final Answer: "It's 72°F and sunny"
```

---

## Build Process - Step by Step

### Step 1: Basic Agent (No Tools)

**Goal:** Simple conversational agent

```python
from anthropic import Anthropic

class BasicAgent:
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
        self.conversation_history = []
    
    def chat(self, user_message):
        """Simple back-and-forth conversation"""
        # Add user message to history
        self.conversation_history.append({
            "role": "user",
            "content": user_message
        })
        
        # Get response from Claude
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=1024,
            messages=self.conversation_history
        )
        
        # Extract assistant response
        assistant_message = response.content[0].text
        
        # Add to history
        self.conversation_history.append({
            "role": "assistant",
            "content": assistant_message
        })
        
        return assistant_message

# Usage
agent = BasicAgent(api_key="your-key")
print(agent.chat("Hello! What can you help me with?"))
print(agent.chat("Tell me a joke"))
```

**Key Concepts:**
- Maintains conversation history
- Simple request-response pattern
- No external capabilities

---

### Step 2: Add Tool Calling

**Goal:** Agent that can use tools to answer questions

```python
import json
from datetime import datetime

class ToolAgent:
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
        self.conversation_history = []
        
        # Define available tools
        self.tools = [
            {
                "name": "get_weather",
                "description": "Get current weather for a location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "City name, e.g., 'San Francisco'"
                        }
                    },
                    "required": ["location"]
                }
            },
            {
                "name": "search_web",
                "description": "Search the web for information",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "Search query"
                        }
                    },
                    "required": ["query"]
                }
            },
            {
                "name": "get_current_time",
                "description": "Get the current time",
                "input_schema": {
                    "type": "object",
                    "properties": {}
                }
            }
        ]
    
    def execute_tool(self, tool_name, tool_input):
        """Execute the actual tool functions"""
        if tool_name == "get_weather":
            # Simulate API call
            location = tool_input["location"]
            return f"Weather in {location}: 72°F, Sunny"
        
        elif tool_name == "search_web":
            # Simulate web search
            query = tool_input["query"]
            return f"Search results for '{query}': [Mock results]"
        
        elif tool_name == "get_current_time":
            return datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        
        else:
            return f"Unknown tool: {tool_name}"
    
    def chat(self, user_message):
        """Chat with tool calling capability"""
        # Add user message
        self.conversation_history.append({
            "role": "user",
            "content": user_message
        })
        
        while True:
            # Get response from Claude
            response = self.client.messages.create(
                model="claude-sonnet-4-20250514",
                max_tokens=4096,
                tools=self.tools,
                messages=self.conversation_history
            )
            
            # Check if Claude wants to use a tool
            if response.stop_reason == "tool_use":
                # Extract tool use blocks
                assistant_content = response.content
                
                # Add assistant's response to history
                self.conversation_history.append({
                    "role": "assistant",
                    "content": assistant_content
                })
                
                # Execute each tool and collect results
                tool_results = []
                for block in assistant_content:
                    if block.type == "tool_use":
                        tool_name = block.name
                        tool_input = block.input
                        
                        print(f"🔧 Using tool: {tool_name}")
                        print(f"   Input: {tool_input}")
                        
                        # Execute the tool
                        result = self.execute_tool(tool_name, tool_input)
                        
                        print(f"   Result: {result}\n")
                        
                        tool_results.append({
                            "type": "tool_result",
                            "tool_use_id": block.id,
                            "content": result
                        })
                
                # Add tool results to history
                self.conversation_history.append({
                    "role": "user",
                    "content": tool_results
                })
                
                # Continue loop to get final response
                continue
            
            else:
                # No more tools needed, return final response
                final_response = ""
                for block in response.content:
                    if hasattr(block, "text"):
                        final_response += block.text
                
                self.conversation_history.append({
                    "role": "assistant",
                    "content": final_response
                })
                
                return final_response

# Usage
agent = ToolAgent(api_key="your-key")
print(agent.chat("What's the weather in San Francisco?"))
print(agent.chat("What time is it?"))
```

**Key Concepts:**
- Tool definitions with schemas
- ReAct loop: reason → act → observe → reason
- Tool execution and result handling
- Conversation continues until no more tools needed

---

### Step 3: Add Memory System

**Goal:** Agent that remembers past conversations

```python
import chromadb
from chromadb.config import Settings

class MemoryAgent:
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
        self.conversation_history = []
        
        # Initialize vector database for long-term memory
        self.chroma_client = chromadb.Client(Settings(
            anonymized_telemetry=False
        ))
        
        # Create collection for memories
        self.memory_collection = self.chroma_client.get_or_create_collection(
            name="agent_memory"
        )
        
        self.memory_counter = 0
    
    def store_memory(self, content, metadata=None):
        """Store information in long-term memory"""
        self.memory_counter += 1
        
        self.memory_collection.add(
            documents=[content],
            metadatas=[metadata or {}],
            ids=[f"memory_{self.memory_counter}"]
        )
        
        print(f"💾 Stored memory: {content[:50]}...")
    
    def retrieve_memories(self, query, n_results=3):
        """Retrieve relevant memories"""
        results = self.memory_collection.query(
            query_texts=[query],
            n_results=n_results
        )
        
        if results['documents']:
            memories = results['documents'][0]
            print(f"🧠 Retrieved {len(memories)} relevant memories")
            return memories
        
        return []
    
    def chat(self, user_message):
        """Chat with memory capability"""
        # Retrieve relevant memories
        relevant_memories = self.retrieve_memories(user_message)
        
        # Build system prompt with memories
        system_prompt = "You are a helpful assistant."
        
        if relevant_memories:
            memory_text = "\n".join([
                f"- {memory}" for memory in relevant_memories
            ])
            system_prompt += f"\n\nRelevant information from past conversations:\n{memory_text}"
        
        # Add user message
        self.conversation_history.append({
            "role": "user",
            "content": user_message
        })
        
        # Get response
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=1024,
            system=system_prompt,
            messages=self.conversation_history
        )
        
        assistant_message = response.content[0].text
        
        # Add to history
        self.conversation_history.append({
            "role": "assistant",
            "content": assistant_message
        })
        
        # Store important information in memory
        self.store_memory(
            f"User said: {user_message}\nAssistant replied: {assistant_message}",
            metadata={"type": "conversation"}
        )
        
        return assistant_message

# Usage
agent = MemoryAgent(api_key="your-key")
print(agent.chat("My name is Alice and I love Python"))
print(agent.chat("What's my name?"))  # Agent remembers!
```

**Key Concepts:**
- Short-term memory: conversation_history
- Long-term memory: vector database (ChromaDB)
- Semantic search for relevant context
- Automatic memory storage and retrieval

---

### Step 4: Add Planning Capability

**Goal:** Agent that can break down complex tasks

```python
class PlanningAgent:
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
        self.tools = self._define_tools()
    
    def _define_tools(self):
        """Define available tools"""
        return [
            {
                "name": "create_plan",
                "description": "Create a step-by-step plan for a complex task",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "task": {"type": "string"},
                        "steps": {
                            "type": "array",
                            "items": {"type": "string"}
                        }
                    },
                    "required": ["task", "steps"]
                }
            },
            {
                "name": "execute_step",
                "description": "Execute a single step from the plan",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "step": {"type": "string"},
                        "step_number": {"type": "integer"}
                    },
                    "required": ["step", "step_number"]
                }
            }
        ]
    
    def execute_complex_task(self, task):
        """Execute a complex task with planning"""
        print(f"📋 Task: {task}\n")
        
        # Step 1: Create plan
        print("🤔 Creating plan...")
        plan_prompt = f"""Create a detailed step-by-step plan to accomplish this task:
        
Task: {task}

Use the create_plan tool to structure your plan."""
        
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=2048,
            tools=self.tools,
            messages=[{"role": "user", "content": plan_prompt}]
        )
        
        # Extract plan
        plan = None
        for block in response.content:
            if block.type == "tool_use" and block.name == "create_plan":
                plan = block.input
                break
        
        if not plan:
            return "Failed to create plan"
        
        print(f"\n📝 Plan created with {len(plan['steps'])} steps:")
        for i, step in enumerate(plan['steps'], 1):
            print(f"  {i}. {step}")
        
        # Step 2: Execute each step
        results = []
        for i, step in enumerate(plan['steps'], 1):
            print(f"\n⚙️  Executing step {i}: {step}")
            
            # Simulate step execution
            step_result = f"Completed: {step}"
            results.append(step_result)
            print(f"   ✅ {step_result}")
        
        # Step 3: Summarize results
        print("\n📊 Task completed! Summary:")
        return {
            "task": task,
            "plan": plan,
            "results": results,
            "status": "completed"
        }

# Usage
agent = PlanningAgent(api_key="your-key")
result = agent.execute_complex_task(
    "Research and write a blog post about AI agents"
)
```

**Key Concepts:**
- Task decomposition
- Sequential execution
- Progress tracking
- Result aggregation

---

### Step 5: Multi-Agent System

**Goal:** Multiple specialized agents working together

```python
class SpecializedAgent:
    """Base class for specialized agents"""
    def __init__(self, api_key, role, expertise):
        self.client = Anthropic(api_key=api_key)
        self.role = role
        self.expertise = expertise
    
    def process(self, task, context=""):
        """Process a task within the agent's expertise"""
        system_prompt = f"""You are a {self.role} with expertise in {self.expertise}.
        
{context}

Process the following task according to your role."""
        
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=2048,
            system=system_prompt,
            messages=[{"role": "user", "content": task}]
        )
        
        return response.content[0].text


class MultiAgentSystem:
    def __init__(self, api_key):
        self.api_key = api_key
        
        # Create specialized agents
        self.researcher = SpecializedAgent(
            api_key,
            role="Research Analyst",
            expertise="gathering and analyzing information from various sources"
        )
        
        self.writer = SpecializedAgent(
            api_key,
            role="Content Writer",
            expertise="creating clear, engaging written content"
        )
        
        self.reviewer = SpecializedAgent(
            api_key,
            role="Quality Reviewer",
            expertise="reviewing content for accuracy, clarity, and quality"
        )
    
    def collaborate(self, task):
        """Multiple agents collaborate on a task"""
        print(f"🎯 Multi-agent task: {task}\n")
        
        # Step 1: Research
        print("🔍 Researcher working...")
        research = self.researcher.process(
            f"Research the following topic and provide key findings:\n{task}"
        )
        print(f"Research complete: {len(research)} chars\n")
        
        # Step 2: Write
        print("✍️  Writer working...")
        draft = self.writer.process(
            f"Write content based on this research:\n{research}",
            context="The researcher has provided the following findings."
        )
        print(f"Draft complete: {len(draft)} chars\n")
        
        # Step 3: Review
        print("👀 Reviewer working...")
        review = self.reviewer.process(
            f"Review this content and provide feedback:\n{draft}",
            context="This content was written based on research findings."
        )
        print(f"Review complete\n")
        
        # Step 4: Revise (Writer incorporates feedback)
        print("✍️  Writer revising...")
        final = self.writer.process(
            f"Revise your content based on this review:\n{review}\n\nOriginal content:\n{draft}"
        )
        
        return {
            "research": research,
            "initial_draft": draft,
            "review": review,
            "final_content": final
        }

# Usage
system = MultiAgentSystem(api_key="your-key")
result = system.collaborate("Write about the benefits of exercise")
print("\n📄 Final content:")
print(result["final_content"])
```

**Key Concepts:**
- Agent specialization
- Sequential collaboration
- Context passing between agents
- Iterative refinement

---

## Tools & Function Calling

### Essential Tool Categories

```python
class ComprehensiveTools:
    """Complete toolkit for AI agents"""
    
    @staticmethod
    def web_tools():
        """Web interaction tools"""
        return [
            {
                "name": "web_search",
                "description": "Search the web",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "query": {"type": "string"}
                    }
                }
            },
            {
                "name": "fetch_webpage",
                "description": "Fetch and parse webpage content",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "url": {"type": "string"}
                    }
                }
            }
        ]
    
    @staticmethod
    def data_tools():
        """Data processing tools"""
        return [
            {
                "name": "query_database",
                "description": "Query SQL database",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "query": {"type": "string"}
                    }
                }
            },
            {
                "name": "analyze_csv",
                "description": "Analyze CSV data",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "file_path": {"type": "string"},
                        "operation": {"type": "string"}
                    }
                }
            }
        ]
    
    @staticmethod
    def code_tools():
        """Code execution tools"""
        return [
            {
                "name": "execute_python",
                "description": "Execute Python code safely",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "code": {"type": "string"}
                    }
                }
            },
            {
                "name": "run_bash",
                "description": "Execute bash commands",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "command": {"type": "string"}
                    }
                }
            }
        ]
    
    @staticmethod
    def file_tools():
        """File system tools"""
        return [
            {
                "name": "read_file",
                "description": "Read file contents",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "path": {"type": "string"}
                    }
                }
            },
            {
                "name": "write_file",
                "description": "Write to file",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "path": {"type": "string"},
                        "content": {"type": "string"}
                    }
                }
            }
        ]

# Combine all tools
def get_all_tools():
    tools = ComprehensiveTools()
    return (tools.web_tools() + 
            tools.data_tools() + 
            tools.code_tools() + 
            tools.file_tools())
```

### Tool Execution Best Practices

```python
import traceback
from typing import Any, Dict

class SafeToolExecutor:
    """Execute tools safely with error handling"""
    
    def execute(self, tool_name: str, tool_input: Dict[str, Any]) -> str:
        """Execute tool with comprehensive error handling"""
        try:
            # Log tool execution
            self._log_execution(tool_name, tool_input)
            
            # Validate input
            self._validate_input(tool_name, tool_input)
            
            # Execute tool
            result = self._execute_tool(tool_name, tool_input)
            
            # Validate output
            self._validate_output(result)
            
            return result
            
        except Exception as e:
            error_msg = self._handle_error(tool_name, e)
            return error_msg
    
    def _log_execution(self, tool_name: str, tool_input: Dict):
        """Log tool execution for debugging"""
        print(f"🔧 Executing: {tool_name}")
        print(f"   Input: {tool_input}")
    
    def _validate_input(self, tool_name: str, tool_input: Dict):
        """Validate tool input against schema"""
        # Add schema validation logic
        pass
    
    def _execute_tool(self, tool_name: str, tool_input: Dict) -> str:
        """Execute the actual tool"""
        # Route to appropriate tool function
        if tool_name == "web_search":
            return self._web_search(tool_input)
        elif tool_name == "execute_python":
            return self._execute_python(tool_input)
        # ... other tools
        
        raise ValueError(f"Unknown tool: {tool_name}")
    
    def _validate_output(self, result: str):
        """Validate tool output"""
        if not result or len(result) > 100000:
            raise ValueError("Invalid tool output")
    
    def _handle_error(self, tool_name: str, error: Exception) -> str:
        """Handle tool execution errors gracefully"""
        error_trace = traceback.format_exc()
        print(f"❌ Error in {tool_name}: {str(error)}")
        
        return f"Tool execution failed: {str(error)}"
    
    def _web_search(self, tool_input: Dict) -> str:
        """Example tool implementation"""
        query = tool_input.get("query", "")
        # Implement actual search logic
        return f"Search results for: {query}"
    
    def _execute_python(self, tool_input: Dict) -> str:
        """Safely execute Python code"""
        code = tool_input.get("code", "")
        
        # Sandboxing, timeout, restrictions
        # This is simplified - use proper sandboxing in production
        
        try:
            # Execute in restricted namespace
            namespace = {"__builtins__": {}}
            exec(code, namespace)
            return str(namespace.get("result", "Code executed"))
        except Exception as e:
            return f"Execution error: {str(e)}"
```

---

## Memory Systems

### Types of Memory

```python
class HybridMemory:
    """Combines short-term and long-term memory"""
    
    def __init__(self):
        # Short-term: Recent conversation
        self.short_term = []
        self.max_short_term = 10
        
        # Long-term: Vector database
        self.long_term = chromadb.Client()
        self.collection = self.long_term.create_collection("memories")
        
        # Working memory: Current task context
        self.working_memory = {}
    
    def add_to_short_term(self, message: Dict):
        """Add to recent conversation history"""
        self.short_term.append(message)
        
        # Trim if too long
        if len(self.short_term) > self.max_short_term:
            # Move to long-term
            old_message = self.short_term.pop(0)
            self.add_to_long_term(old_message)
    
    def add_to_long_term(self, content: str, metadata: Dict = None):
        """Store in vector database"""
        self.collection.add(
            documents=[content],
            metadatas=[metadata or {}],
            ids=[f"mem_{len(self.collection.get()['ids'])}"]
        )
    
    def retrieve_relevant(self, query: str, k: int = 5) -> list:
        """Retrieve relevant long-term memories"""
        results = self.collection.query(
            query_texts=[query],
            n_results=k
        )
        return results['documents'][0] if results['documents'] else []
    
    def update_working_memory(self, key: str, value: Any):
        """Update current task context"""
        self.working_memory[key] = value
    
    def get_context(self, query: str) -> str:
        """Build complete context for LLM"""
        context_parts = []
        
        # Add working memory
        if self.working_memory:
            context_parts.append("Current context:")
            for key, value in self.working_memory.items():
                context_parts.append(f"- {key}: {value}")
        
        # Add short-term memory
        if self.short_term:
            context_parts.append("\nRecent conversation:")
            for msg in self.short_term[-5:]:
                role = msg.get("role", "unknown")
                content = msg.get("content", "")
                context_parts.append(f"{role}: {content[:100]}")
        
        # Add relevant long-term memories
        relevant = self.retrieve_relevant(query, k=3)
        if relevant:
            context_parts.append("\nRelevant memories:")
            for mem in relevant:
                context_parts.append(f"- {mem[:100]}")
        
        return "\n".join(context_parts)
```

### Memory Consolidation

```python
class MemoryConsolidator:
    """Summarize and consolidate memories over time"""
    
    def __init__(self, client):
        self.client = client
    
    def consolidate_conversation(self, messages: list) -> str:
        """Summarize a long conversation"""
        conversation_text = "\n".join([
            f"{msg['role']}: {msg['content']}" 
            for msg in messages
        ])
        
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=500,
            messages=[{
                "role": "user",
                "content": f"""Summarize the key points from this conversation:

{conversation_text}

Focus on:
1. Main topics discussed
2. Decisions made
3. Action items
4. Important facts to remember"""
            }]
        )
        
        return response.content[0].text
    
    def extract_facts(self, text: str) -> list:
        """Extract factual statements to remember"""
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=500,
            messages=[{
                "role": "user",
                "content": f"""Extract factual statements from this text that should be remembered:

{text}

Return a list of facts, one per line."""
            }]
        )
        
        facts = response.content[0].text.strip().split("\n")
        return [f.strip() for f in facts if f.strip()]
```

---

## Agent Frameworks Comparison

### LangChain

```python
from langchain.agents import create_react_agent
from langchain.tools import Tool
from langchain_anthropic import ChatAnthropic

# Define tools
tools = [
    Tool(
        name="Search",
        func=lambda q: f"Search results for {q}",
        description="Search the web"
    )
]

# Create agent
llm = ChatAnthropic(model="claude-sonnet-4-20250514")
agent = create_react_agent(llm, tools, prompt)
```

**Pros:**
- Extensive tool ecosystem
- Good documentation
- Active community

**Cons:**
- Can be complex/bloated
- Abstraction overhead
- Harder to debug

### LlamaIndex

```python
from llama_index.core.agent import ReActAgent
from llama_index.llms.anthropic import Anthropic
from llama_index.core.tools import FunctionTool

# Define tools
def search(query: str) -> str:
    return f"Results for {query}"

search_tool = FunctionTool.from_defaults(fn=search)

# Create agent
llm = Anthropic(model="claude-sonnet-4-20250514")
agent = ReActAgent.from_tools([search_tool], llm=llm)
```

**Pros:**
- Great for RAG applications
- Clean abstractions
- Good for data-heavy apps

**Cons:**
- More focused on retrieval
- Smaller ecosystem
- Less flexible for custom workflows

### Custom (Recommended for Production)

```python
# Build your own for full control
class ProductionAgent:
    def __init__(self, config):
        self.client = Anthropic(api_key=config.api_key)
        self.tools = self._load_tools(config.tools)
        self.memory = HybridMemory()
        self.executor = SafeToolExecutor()
    
    def run(self, task):
        """Full control over execution logic"""
        # Your custom implementation
        pass
```

**Pros:**
- Complete control
- Easy to debug
- Optimized for your needs
- No framework lock-in

**Cons:**
- More code to write
- Need to implement everything
- Maintenance burden

---

## Production Deployment

### Architecture for Scale

```python
from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
import redis
from celery import Celery

app = FastAPI()
redis_client = redis.Redis()
celery_app = Celery('agent', broker='redis://localhost:6379')

class AgentRequest(BaseModel):
    task: str
    user_id: str

class ProductionAgentSystem:
    """Production-ready agent system"""
    
    def __init__(self):
        self.agent = ToolAgent(api_key="your-key")
        
    @celery_app.task
    def process_async(self, task: str, user_id: str):
        """Process agent task asynchronously"""
        try:
            # Execute agent
            result = self.agent.chat(task)
            
            # Store result
            redis_client.set(
                f"result:{user_id}",
                result,
                ex=3600  # Expire in 1 hour
            )
            
            return {"status": "completed", "user_id": user_id}
            
        except Exception as e:
            return {"status": "failed", "error": str(e)}

@app.post("/agent/task")
async def create_task(request: AgentRequest, background_tasks: BackgroundTasks):
    """API endpoint for agent tasks"""
    
    # Queue task
    task = ProductionAgentSystem().process_async.delay(
        request.task,
        request.user_id
    )
    
    return {
        "task_id": task.id,
        "status": "queued"
    }

@app.get("/agent/result/{user_id}")
async def get_result(user_id: str):
    """Get task result"""
    result = redis_client.get(f"result:{user_id}")
    
    if result:
        return {"result": result.decode()}
    else:
        return {"status": "processing"}
```

### Monitoring & Observability

```python
import logging
from prometheus_client import Counter, Histogram
import time

# Metrics
tool_calls = Counter('agent_tool_calls', 'Tool calls', ['tool_name'])
response_time = Histogram('agent_response_time', 'Response time')
errors = Counter('agent_errors', 'Errors', ['error_type'])

class MonitoredAgent:
    """Agent with comprehensive monitoring"""
    
    def __init__(self, api_key):
        self.agent = ToolAgent(api_key)
        self.logger = logging.getLogger(__name__)
    
    @response_time.time()
    def chat(self, message):
        """Chat with monitoring"""
        try:
            self.logger.info(f"Processing message: {message[:50]}")
            
            start_time = time.time()
            result = self.agent.chat(message)
            duration = time.time() - start_time
            
            self.logger.info(f"Completed in {duration:.2f}s")
            
            return result
            
        except Exception as e:
            errors.labels(error_type=type(e).__name__).inc()
            self.logger.error(f"Error: {str(e)}", exc_info=True)
            raise
    
    def execute_tool(self, tool_name, tool_input):
        """Execute tool with metrics"""
        tool_calls.labels(tool_name=tool_name).inc()
        
        self.logger.info(f"Executing tool: {tool_name}")
        
        return self.agent.execute_tool(tool_name, tool_input)
```

### Rate Limiting & Cost Control

```python
from functools import wraps
import time

class RateLimiter:
    """Token bucket rate limiter"""
    
    def __init__(self, rate: int, per: int):
        self.rate = rate  # requests
        self.per = per    # seconds
        self.allowance = rate
        self.last_check = time.time()
    
    def allow_request(self) -> bool:
        """Check if request is allowed"""
        current = time.time()
        time_passed = current - self.last_check
        self.last_check = current
        
        self.allowance += time_passed * (self.rate / self.per)
        
        if self.allowance > self.rate:
            self.allowance = self.rate
        
        if self.allowance < 1.0:
            return False
        
        self.allowance -= 1.0
        return True

def rate_limit(limiter: RateLimiter):
    """Rate limiting decorator"""
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            if not limiter.allow_request():
                raise Exception("Rate limit exceeded")
            return func(*args, **kwargs)
        return wrapper
    return decorator

# Usage
limiter = RateLimiter(rate=10, per=60)  # 10 requests per minute

class RateLimitedAgent:
    def __init__(self, api_key):
        self.agent = ToolAgent(api_key)
    
    @rate_limit(limiter)
    def chat(self, message):
        return self.agent.chat(message)
```

---

## Advanced Patterns

### 1. Self-Healing Agent

```python
class SelfHealingAgent:
    """Agent that can recover from errors"""
    
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
        self.max_retries = 3
    
    def execute_with_recovery(self, task):
        """Execute task with automatic error recovery"""
        
        for attempt in range(self.max_retries):
            try:
                return self._execute(task)
                
            except Exception as e:
                print(f"⚠️  Attempt {attempt + 1} failed: {str(e)}")
                
                if attempt < self.max_retries - 1:
                    # Ask agent to fix the error
                    fix_prompt = f"""The previous attempt failed with this error:
{str(e)}

Original task: {task}

Please provide a corrected approach."""
                    
                    response = self.client.messages.create(
                        model="claude-sonnet-4-20250514",
                        max_tokens=1024,
                        messages=[{"role": "user", "content": fix_prompt}]
                    )
                    
                    # Try corrected approach
                    task = response.content[0].text
                else:
                    raise
    
    def _execute(self, task):
        """Execute the task"""
        # Implementation
        pass
```

### 2. Hierarchical Agent

```python
class HierarchicalAgent:
    """Manager agent that delegates to worker agents"""
    
    def __init__(self, api_key):
        self.manager = SpecializedAgent(
            api_key,
            role="Project Manager",
            expertise="task planning and delegation"
        )
        
        self.workers = {
            "researcher": SpecializedAgent(api_key, "Researcher", "research"),
            "analyst": SpecializedAgent(api_key, "Analyst", "analysis"),
            "writer": SpecializedAgent(api_key, "Writer", "writing")
        }
    
    def execute(self, task):
        """Manager delegates to workers"""
        
        # Manager creates plan
        plan = self.manager.process(
            f"Break down this task and assign to: {list(self.workers.keys())}\n\n{task}"
        )
        
        print(f"📋 Manager's plan:\n{plan}\n")
        
        # Parse plan and delegate (simplified)
        # In production, use structured output
        
        results = {}
        for worker_name, worker in self.workers.items():
            subtask = f"Complete your part of: {task}"
            results[worker_name] = worker.process(subtask)
        
        # Manager synthesizes results
        synthesis = self.manager.process(
            f"Synthesize these results:\n\n" +
            "\n\n".join([f"{k}: {v}" for k, v in results.items()])
        )
        
        return synthesis
```

### 3. Learning Agent

```python
class LearningAgent:
    """Agent that learns from feedback"""
    
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
        self.feedback_history = []
    
    def execute_with_feedback(self, task):
        """Execute task and learn from feedback"""
        
        # Get relevant past feedback
        context = self._get_learning_context(task)
        
        # Execute with learned knowledge
        system_prompt = f"""Based on past feedback:
{context}

Apply these learnings to the current task."""
        
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=2048,
            system=system_prompt,
            messages=[{"role": "user", "content": task}]
        )
        
        return response.content[0].text
    
    def add_feedback(self, task, result, feedback):
        """Store feedback for learning"""
        self.feedback_history.append({
            "task": task,
            "result": result,
            "feedback": feedback,
            "timestamp": time.time()
        })
    
    def _get_learning_context(self, current_task):
        """Get relevant past feedback"""
        # Simplified - use vector similarity in production
        recent = self.feedback_history[-5:]
        
        return "\n".join([
            f"- Task: {f['task'][:50]}...\n  Feedback: {f['feedback']}"
            for f in recent
        ])
```

---

## Real-World Examples

### Example 1: Customer Support Agent

```python
class CustomerSupportAgent:
    """Automated customer support"""
    
    def __init__(self, api_key, knowledge_base):
        self.client = Anthropic(api_key=api_key)
        self.kb = knowledge_base
        self.tools = [
            {
                "name": "search_kb",
                "description": "Search knowledge base for answers",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "query": {"type": "string"}
                    }
                }
            },
            {
                "name": "create_ticket",
                "description": "Create support ticket for human review",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "issue": {"type": "string"},
                        "priority": {"type": "string"}
                    }
                }
            }
        ]
    
    def handle_inquiry(self, customer_message):
        """Handle customer support inquiry"""
        
        system = """You are a helpful customer support agent. 
        
Guidelines:
- Be empathetic and professional
- Search knowledge base first
- Escalate complex issues
- Provide clear, actionable answers"""
        
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=2048,
            system=system,
            tools=self.tools,
            messages=[{"role": "user", "content": customer_message}]
        )
        
        return self._process_response(response)
```

### Example 2: Data Analysis Agent

```python
class DataAnalysisAgent:
    """Automated data analysis"""
    
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
    
    def analyze_dataset(self, data_path):
        """Complete data analysis pipeline"""
        
        # Step 1: Load and explore
        exploration = self._explore_data(data_path)
        
        # Step 2: Generate insights
        insights = self._generate_insights(exploration)
        
        # Step 3: Create visualizations
        viz = self._create_visualizations(data_path, insights)
        
        # Step 4: Write report
        report = self._write_report(insights, viz)
        
        return report
    
    def _explore_data(self, data_path):
        """Exploratory data analysis"""
        code = f"""
import pandas as pd

df = pd.read_csv('{data_path}')
summary = df.describe()
dtypes = df.dtypes
missing = df.isnull().sum()
"""
        # Execute and return results
        pass
```

### Example 3: Code Review Agent

```python
class CodeReviewAgent:
    """Automated code review"""
    
    def __init__(self, api_key):
        self.client = Anthropic(api_key=api_key)
    
    def review_pr(self, diff, context=""):
        """Review pull request"""
        
        system = """You are an experienced code reviewer.

Review for:
- Bugs and potential issues
- Code quality and readability
- Performance concerns
- Security vulnerabilities
- Best practices

Provide constructive feedback."""
        
        response = self.client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=4096,
            system=system,
            messages=[{
                "role": "user",
                "content": f"Context: {context}\n\nCode diff:\n{diff}"
            }]
        )
        
        return self._format_review(response.content[0].text)
```

---

## Best Practices Checklist

### Development
- [ ] Start simple, add complexity gradually
- [ ] Use type hints and docstrings
- [ ] Implement comprehensive error handling
- [ ] Log all tool executions
- [ ] Write unit tests for tools
- [ ] Use async/await for I/O operations

### Production
- [ ] Implement rate limiting
- [ ] Add monitoring and alerting
- [ ] Set up cost tracking
- [ ] Use message queues for async tasks
- [ ] Implement graceful degradation
- [ ] Cache LLM responses when appropriate
- [ ] Set up A/B testing for prompts

### Security
- [ ] Validate all tool inputs
- [ ] Sandbox code execution
- [ ] Implement access controls
- [ ] Encrypt sensitive data
- [ ] Audit tool usage
- [ ] Set resource limits

---

## Resources & Next Steps

### Learning Path
1. Build basic conversational agent
2. Add 2-3 simple tools
3. Implement memory system
4. Create multi-agent system
5. Deploy to production

### Further Reading
- Anthropic Tool Use Documentation
- ReAct Paper (Reasoning + Acting)
- LangChain Agent Documentation
- Production ML Systems Design

### Community
- Anthropic Discord
- r/MachineLearning
- AI Agent Development Forums

---

**Happy building! 🚀**

Remember: Start simple, iterate quickly, and always prioritize reliability over complexity.