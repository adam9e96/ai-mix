"use client";

import { TypeAnimation } from "react-type-animation";

export default function TypingText({
  text = [],
  typingSpeed = 50,
  pauseDuration = 500,
  showCursor = true,
  cursorCharacter = "|",
  className = "",
}) {
  const sequence = [];

  text.forEach((t) => {
    sequence.push(t, pauseDuration, () => {});
  });

  return (
    <div className={className}>
      <TypeAnimation
        sequence={sequence}
        speed={typingSpeed}
        repeat={0}
        cursor={showCursor}
        wrapper="span"
        cursorStyle={cursorCharacter}
        style={{
          display: "inline-block",
          whiteSpace: "pre-line",
        }}
      />
    </div>
  );
}

