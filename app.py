import gradio as gr
from model_manager import get_model_path
from llm_engine import init_engine, generate


def main():
    print("Initializing model manager...")
    model_path = get_model_path()

    print("Loading LLM engine (this may take a moment)...")
    engine = init_engine(model_path)
    print("Engine ready.")

    def chat_fn(message, history):
        history_dicts = []
        for msg in history:
            history_dicts.append({"role": msg["role"], "content": msg["content"]})
        yield from generate(engine, message, history_dicts)

    demo = gr.ChatInterface(
        fn=chat_fn,
        title="Local Chatbot",
        description="A local chatbot powered by Qwen-AgentWorld-35B",
    )

    print("Launching browser...")
    demo.launch(inbrowser=True)


if __name__ == "__main__":
    main()
