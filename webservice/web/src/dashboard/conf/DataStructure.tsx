export interface StatisticsResponse {
  status: string;
  totalDocs: number;
  compStats: {
    [component: string]: number,
  };
  personStats: PersonState[];
  totalQueries: number;
  intervalQueries: IntervalState[];
  topAnswers: DocItem[];
  topQueries: string[];
}

export interface DocItem {
  id: string;
  userName: string;
  component: string;
  problem: string;
  solution: string;
}

export interface PersonState {
  name: string;
  count: number;
}

export interface IntervalState {
  timestamp: number;
  count: number;
}
