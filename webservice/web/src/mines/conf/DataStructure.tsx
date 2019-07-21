export interface DocItem {
  id: string;
  userName: string;
  component: string;
  problem: string;
  solution: string;
}

export interface SearchResponse {
  docs: DocItem[];
}
