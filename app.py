import gradio as gr
from model_manager import get_model_path
from llm_engine import init_engine, generate


def main():
    print("Initializing model manager...")
    model_path = get_model_path()

    print("Loading LLM engine (this may take a moment)...")
    engine = init_engine(model_path)
    print("Engine ready.")

    def get_content(msg):
        if isinstance(msg, dict):
            content = msg.get("content", "")
            if isinstance(content, list):
                texts = [item.get("text", "") if isinstance(item, dict) else str(item) for item in content]
                return " ".join(texts)
            return content
        if hasattr(msg, "text"):
            return msg.text
        return str(msg)

    def get_role(msg):
        if isinstance(msg, dict):
            return msg.get("role", "user")
        if hasattr(msg, "role"):
            return msg.role
        return "user"

    def respond(message, history, show_thinking):
        if not message.strip():
            yield history, "", ""
            return

        # Check if the last message in history is already the current user message
        if history and get_role(history[-1]) == "user" and get_content(history[-1]) == message:
            # History already contains this message, just add assistant placeholder
            history = history + [{"role": "assistant", "content": ""}]
        else:
            history = history + [
                {"role": "user", "content": message},
                {"role": "assistant", "content": ""},
            ]

        llm_history = [{"role": get_role(h), "content": get_content(h)} for h in history[:-2]]

        for thinking, answer, in_thinking, thinking_done in generate(
            engine, message, llm_history
        ):
            if in_thinking:
                bot_msg = "⏳ Thinking..."
            else:
                bot_msg = answer if answer else "..."

            yield history[:-1] + [{"role": "assistant", "content": bot_msg}], "", thinking

        final_answer = answer if answer else "No response generated."
        yield history[:-1] + [{"role": "assistant", "content": final_answer}], "", thinking

    MODEL_URL = "https://huggingface.co/unsloth/Qwen3.5-4B-GGUF"

    with gr.Blocks(title="Local Chatbot") as demo:
        gr.Markdown(f"# QL\na local chatbot powered by [Qwen3.5-4B]({MODEL_URL})")

        with gr.Row():
            with gr.Column(scale=3):
                chatbot = gr.Chatbot(height=500)
            with gr.Column(scale=1, visible=False) as thinking_col:
                thinking_display = gr.Markdown(label="Thinking Process", height=500)

        with gr.Row():
            msg = gr.Textbox(placeholder="Type your message...", scale=4, show_label=False)
            submit = gr.Button("Send", scale=1)

        show_thinking = gr.Checkbox(label="Show thinking process", value=False)

        show_thinking.change(lambda show: gr.update(visible=show), [show_thinking], [thinking_col])

        msg.submit(respond, [msg, chatbot, show_thinking], [chatbot, msg, thinking_display])
        submit.click(respond, [msg, chatbot, show_thinking], [chatbot, msg, thinking_display])

    print("Launching browser...")
    demo.launch(inbrowser=True)


if __name__ == "__main__":
    main()
