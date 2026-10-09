import { useLocation } from "react-router";

// cada integrante registra aqui o titulo e o caminho da tela dele.
const titulosPorRota: Record<string, { titulo: string; caminho: string }> = {
  "/usuarios": { titulo: "Usuários", caminho: "Administração · Usuários" },
  "/clientes": { titulo: "Clientes", caminho: "Carteira · Clientes" },
  "/funil": { titulo: "Funil de oportunidades", caminho: "Vendas · Funil" },
  "/tarefas": { titulo: "Tarefas", caminho: "Agenda · Tarefas" },
};

export default function Topbar() {
  // o useLocation devolve a url aberta agora.
  const { pathname } = useLocation();
  const atual = titulosPorRota[pathname] ?? { titulo: "miniCRM", caminho: "" };

  return (
    <header className="sticky top-0 z-20 flex h-[60px] flex-none items-center justify-between border-b-2 border-line bg-surface px-6">
      <div className="min-w-0">
        <h1 className="text-[21px]">{atual.titulo}</h1>
        <div className="mt-px text-xs text-muted">{atual.caminho}</div>
      </div>
    </header>
  );
}
