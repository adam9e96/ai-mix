import { useEffect, useState } from "react";
import "../../styles/components/LoadingDots.css";

export default function LoadingDots() {
  const [dots, setDots] = useState("");

  useEffect(() => {
    const interval = setInterval(() => {
      setDots((prev) => (prev.length >= 3 ? "" : prev + "."));
    }, 400);

    return () => clearInterval(interval);
  }, []);

  return <span className="loading-dots">{dots}</span>;
}

