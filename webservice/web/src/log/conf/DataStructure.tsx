export interface LogResponse {
}

export interface LogInitResponse {
  status: string;
  rate: number;
  compSet: string[];
}

export interface InceptorDateResponse extends LogResponse {
  dates: DateBean[];
}

export interface InceptorTimelineResponse extends LogResponse {
  goalsBySession: SessionGoalBean[];
  size: number;
}

export interface InceptorSessionResponse {
  sessions: SessionInfoBean[];
  min: number;
  max: number;
}

export interface InceptorFlatGoalsResponse extends LogResponse {
  goals: GoalTaskThinBean[];
  size: number;
}

export interface InceptorGoalResponse extends LogResponse {
  goal: GoalTaskBean;
  goalsBySession: {
    session: string,
    goals: GoalTaskThinBean[],
  };
}

export interface InceptorGoalTimelineResponse {
  targetSession: SessionGoalBean;
  goalsBySession: SessionGoalBean[];
  size: number;
}

export interface DateBean {
  date: number;
  errorNum: number;
  longDurationNum: number;
  normalNum: number;
}

export interface SessionGoalBean {
  id: string;
  goals: GoalTaskThinBean[];
}

export interface SessionInfoBean {
  id: string;
  startTime: number;
  endTime: number;
}

export interface GoalTaskBean {
  id: string;
  name: string;
  desc: string;
  goalStatus: string;
  statusMsg: string;
  duration: number;
  startTime: number;
  endTime: number;
  tasks: TaskBean[];
  entities: LogEntity[];
}

export interface GoalTaskThinBean {
  id: string;
  name: string;
  desc: string;
  goalStatus: string;
  statusMsg: string;
  duration: number;
  startTime: number;
  endTime: number;
  tasks: TaskBean[];
}

export interface TaskBean {
  id: string;
  name: string;
  taskStatus: string;
  errorType: string;
  duration: number;
  desc: string;
  errorIndices: number[];
  startTime: number;
  endTime: number;
  preIndex: number;
  occurIndex: number;
  postIndex: number;
  subTasks: TaskBean[];
}

export interface LogEntity {
  component: string;
  timeStamp: number;
  level: string;
  content: string;
}
