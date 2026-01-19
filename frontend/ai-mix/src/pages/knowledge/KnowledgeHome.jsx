import KnowledgeSidebar from "@components/knowledge/KnowledgeSidebar";
import KnowledgeMain from "@components/knowledge/KnowledgeMain";
import "@styles/pages/knowledge.css";

export default function KnowledgeHome() {
  return (
    <div className="knowledge-page">
      <KnowledgeSidebar />
      <KnowledgeMain />
    </div>
  );
}
