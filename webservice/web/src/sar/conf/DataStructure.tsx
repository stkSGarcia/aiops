export interface SarReportBean {
  errorMsg: string;
  fileName: string;
  titleDesc: string[];
  titleName: string[];
  titleEnabled: boolean[];
  warnLevel: number[];
  totalRecordsNumber: number;
  details: string[];
  wasError: boolean[][];
  errorPercent: string[];
  errorNum: number[];
}
