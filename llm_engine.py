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
    thinking = ""
    answer = ""
    in_thinking = False
    thinking_done = False

    THINK_OPEN = "<think>"
    THINK_CLOSE = "</think>"

    for chunk in stream:
        token = chunk["choices"][0]["text"]
        partial += token

        if THINK_OPEN in partial:
            think_start = partial.index(THINK_OPEN) + len(THINK_OPEN)
            if THINK_CLOSE in partial[think_start:]:
                think_end = partial.index(THINK_CLOSE, think_start)
                thinking = partial[think_start:think_end]
                thinking_done = True
                in_thinking = False
                ans_start = think_end + len(THINK_CLOSE)
                if ans_start < len(partial) and partial[ans_start] == "\n":
                    ans_start += 1
                answer = partial[ans_start:]
            else:
                thinking = partial[think_start:]
                in_thinking = True
                thinking_done = False
        elif not thinking_done:
            answer = partial

        yield thinking, answer, in_thinking, thinking_done

    yield thinking, answer, False, True
