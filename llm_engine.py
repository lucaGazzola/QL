from llama_cpp import Llama

IM_START = "\u003cim_start\u003e"
IM_END = "\u003cim_end\u003e"


def init_engine(model_path: str) -> Llama:
    return Llama(
        model_path=model_path,
        n_gpu_layers=-1,
        n_ctx=8192,
        verbose=False,
    )


def format_prompt(user_message: str, history: list[dict]) -> str:
    prompt = ""
    for entry in history:
        role = entry["role"]
        content = entry["content"]
        prompt += f"{IM_START}{role}\n{content}{IM_END}\n"
    prompt += f"{IM_START}user\n{user_message}{IM_END}\n{IM_START}assistant\n"
    return prompt


def generate(engine: Llama, user_message: str, history: list[dict]):
    prompt = format_prompt(user_message, history)
    stream = engine(
        prompt,
        max_tokens=2048,
        stop=[IM_END],
        stream=True,
    )
    partial = ""
    for chunk in stream:
        token = chunk["choices"][0]["text"]
        partial += token
        yield partial
