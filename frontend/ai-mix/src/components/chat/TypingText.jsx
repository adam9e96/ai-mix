"use client";

import { memo } from "react";
import { TypeAnimation } from "react-type-animation";

// React.memo: 텍스트가 변경되지 않으면 타이핑 애니메이션 재실행 방지
const TypingText = memo(function TypingText({
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
});

export default TypingText;

