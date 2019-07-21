export interface JstackResponse {
  components: string[];
  fileInfo: JstackFileInfo[];
  totalInfo: JstackFileInfo;
}

export interface AnalyzeResponse {
  components: string[];
  fileInfo: JstackFileInfo;
  allHistory: string[];
}

export interface JstackFileInfo {
  fileName: string;
  timestamp: string;
  callStackMap: {
    [callStack: string]: JstackEntryWithLevel,
  };
  noCallStackArray: JstackEntry[];
  lockMap: LockMap;
  methodList: MethodInfo[];
  groupMap: {
    [groupName: string]: JstackEntryWithLevel,
  };
}

export interface EntryMap {
  [name: string]: JstackEntry;
}

export interface LockMap {
  [lockAddress: string]: JstackEntry[][];
}

export interface MethodInfo {
  method: string;
  entryList: JstackEntry[];
}

export interface JstackEntry {
  threadName: string;
  isDaemon: boolean;
  threadState: any;
  locks: Lock[];
  waitLocks: Lock[];
  id: string;
  prio: string;
  os_prio: string;
  tid: string;
  nid: string;
  startLine: string;
  callStack: string;
  method: String;
}

export interface Lock {
  addr: string;
  name: string;
  originLine: string;
}

export interface JstackEntryWithLevel {
  entryList: JstackEntry[];
  level: any;
  keyword: string[];
}

export interface BlackWhiteResponse {
  blackWhiteList: BlackWhiteList;
}

export interface BlackWhiteList {
  whiteList: string[];
  blackList: string[];
}
