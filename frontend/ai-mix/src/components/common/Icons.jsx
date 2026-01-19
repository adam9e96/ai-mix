import { useState } from "react";

/* ============================================================
   🟦 1) BotIcon (챗봇)
============================================================ */
export function BotIcon({ size = 24, color = "currentColor", className = "" }) {
  const [isHovered, setIsHovered] = useState(false);
  const [eyeY1, setEyeY1] = useState(13);
  const [eyeY2, setEyeY2] = useState(15);

  const animateEyes = (s1, s2, e1, e2, duration, delay = 0) =>
    new Promise((resolve) => {
      setTimeout(() => {
        const start = performance.now();
        const animate = (now) => {
          const progress = Math.min((now - start) / duration, 1);
          const ease =
            progress < 0.5
              ? 2 * progress * progress
              : 1 - Math.pow(-2 * progress + 2, 2) / 2;

          setEyeY1(s1 + (e1 - s1) * ease);
          setEyeY2(s2 + (e2 - s2) * ease);

          if (progress < 1) requestAnimationFrame(animate);
          else resolve();
        };
        requestAnimationFrame(animate);
      }, delay);
    });

  const onEnter = () => {
    setIsHovered(true);
    animateEyes(13, 15, 14, 14, 250, 200).then(() =>
      animateEyes(14, 14, 13, 15, 250).then(() => setIsHovered(false))
    );
  };

  return (
    <div onMouseEnter={onEnter} className={className}>
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        stroke={color}
        fill="none"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        className={isHovered ? "bot-animate" : ""}
      >
        <path d="M12 8V4H8" />
        <rect width="16" height="12" x="4" y="8" rx="2" />
        <path d="M2 14h2" />
        <path d="M20 14h2" />
        <line x1="15" y1={eyeY1} x2="15" y2={eyeY2} />
        <line x1="9" y1={eyeY1} x2="9" y2={eyeY2} />
      </svg>
    </div>
  );
}

/* ============================================================
   🟩 2) WikiIcon (지식백과)
============================================================ */
export function WikiIcon({
  size = 24,
  color = "currentColor",
  className = "",
}) {
  const [hover, setHover] = useState(false);

  const onEnter = () => {
    setHover(true);
    setTimeout(() => setHover(false), 600);
  };

  return (
    <div onMouseEnter={onEnter} className={className}>
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        stroke={color}
        fill="none"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        className={hover ? "wiki-animate" : ""}
      >
        <path d="M10 13h4" />
        <path d="M12 6v7" />
        <path d="M16 8V6H8v2" />
        <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H19a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1H6.5a1 1 0 0 1 0-5H20" />
      </svg>
    </div>
  );
}

/* ============================================================
   🟨 3) QnaIcon (Q&A)
============================================================ */
export function QnaIcon({ size = 24, color = "currentColor", className = "" }) {
  const [hover, setHover] = useState(false);

  const onEnter = () => {
    setHover(true);
    setTimeout(() => setHover(false), 500);
  };

  return (
    <div onMouseEnter={onEnter} className={className}>
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        stroke={color}
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <circle cx="12" cy="12" r="10" />
        <g className={hover ? "qna-rotate" : ""}>
          <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
          <path d="M12 17h.01" />
        </g>
      </svg>
    </div>
  );
}

/* ============================================================
   🛡️ 4) ShieldCheckIcon (보안)
============================================================ */
export function ShieldCheckIcon({
  size = 24,
  color = "currentColor",
  className = "",
}) {
  const [hover, setHover] = useState(false);

  const onEnter = () => {
    setHover(true);
    setTimeout(() => setHover(false), 500);
  };

  return (
    <div onMouseEnter={onEnter} className={className}>
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        stroke={color}
        fill="none"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        className={hover ? "shield-animate" : ""}
      >
        <path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z" />
        <path className="shield-check" d="m9 12 2 2 4-4" />
      </svg>
    </div>
  );
}

/* ============================================================
   👁 5) EyeIcon (비밀번호 보이기)
============================================================ */
export function EyeIcon({ size = 22, color = "currentColor", className = "" }) {
  return (
    <svg
      width={size}
      height={size}
      stroke={color}
      fill="none"
      strokeWidth="2"
      viewBox="0 0 24 24"
      className={className}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

/* ============================================================
   👁‍🗨 6) EyeOffIcon (비밀번호 숨기기)
============================================================ */
export function EyeOffIcon({
  size = 22,
  color = "currentColor",
  className = "",
}) {
  return (
    <svg
      width={size}
      height={size}
      stroke={color}
      fill="none"
      strokeWidth="2"
      viewBox="0 0 24 24"
      className={className}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a21.86 21.86 0 0 1 5.12-5.82" />
      <path d="M1 1l22 22" />
      <path d="M10.58 10.58A3 3 0 0 0 13.42 13.42" />
      <path d="M9.88 5.24A10.94 10.94 0 0 1 12 5c7 0 11 7 11 7a21.86 21.86 0 0 1-2.16 3.19" />
    </svg>
  );
}

