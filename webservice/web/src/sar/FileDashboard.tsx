import * as React from 'react';
import * as Style from '../Style';
import { Col, Icon, Row } from 'antd';
import { SarReportBean } from './conf/DataStructure';

interface FileDashboardProps {
  sarReport: SarReportBean;
  sarReportIndex: number;
  history: any;
  locationKey: string;
}

interface FileDashboardState {
  summary: string[][];
  details: string[][];
  visibility: string[];
}

export default class FileDashboard extends React.Component<FileDashboardProps, FileDashboardState> {
  renderSummaryTxt = (i: number, j: number, a: string) => {
    if (j === 0 && this.state.details[i].length > 0) {
      return (
        <span>{a + ' '}
        </span>);
    } else {
      return (a);
    }
  }

  updateStateFromRes = (sarRes: SarReportBean,
                        summaryRecords: string[][],
                        visibilityL: string[]) => {
    var summaryTxts: string[] = sarRes.errorNum.map((v, i) => {
      var s = '';
      if (v > 0) {
        s = sarRes.warnLevel[i] + '|' +
          sarRes.errorPercent[i] + '(' + sarRes.errorNum[i] + '条)' +
          '的数据表征出集群' +
          sarRes.titleName[i] +
          '偏高，这说明该时段' +
          sarRes.titleDesc[i];
      }
      return s;
    });
    summaryTxts = summaryTxts.filter((v) => (v.length !== 0));
    if (summaryTxts.length === 0) {
      summaryTxts = ['0|未发现异常指标'];
    }
    summaryTxts = ['0|sar文件"' + sarRes.fileName + '"中共发现' + sarRes.totalRecordsNumber + '条有效记录']
      .concat(summaryTxts);
    summaryRecords.push(summaryTxts);
    visibilityL.push('none');
    var ds = this.state.details;
    ds.push(sarRes.details);
    this.setState({
      summary: summaryRecords,
      details: ds,
      visibility: visibilityL,
    });
  }

  constructor(props: any) {
    super(props);
    this.state = {
      summary: [],
      details: [],
      visibility: [],
    };
  }

  componentWillMount() {
    var summaryRecords: string[][] = [];
    var visibilityL: string[] = [];
    const sarReport = this.props.sarReport;
    if (sarReport.errorMsg !== null && sarReport.errorMsg.length !== 0) {
      alert(sarReport.errorMsg);
    } else {
      this.updateStateFromRes(sarReport, summaryRecords, visibilityL);
    }
  }

  render() {
    return (
      <div>
        <div style={Style.block}>
          <h3 style={{fontWeight: 'bold', marginBottom: '10px'}}>Summary </h3>
          {(
            this.state.summary.map((ss: string[], i: number, array: string[][]) => {
              return (<div key={'div_' + i}>
                <Row key={'row_' + i} type="flex" justify="start" style={{marginLeft: '40px', marginTop: '20px', width: '700px'}}>
                  <Col>
                    {
                      array[i].map((v: string, j: number) => {
                        var a = v.split('|');
                        return (
                          <span
                            key={'span_' + i + '_' + j}
                            style={a[0] === '0' ? Style.black
                              : a[0] === '1' ? Style.blue
                                : a[0] === '2' ? Style.orange : Style.red
                            }
                          >
                          {this.renderSummaryTxt(i, j, a[1])}<br/>
                          </span>
                        );
                      })
                    }
                  </Col>
                </Row>
              </div>);
            })
          )}
        </div>

        <div style={Style.block}>
          {(this.state.summary.map((ss: string[], i: number, array: string[][]) => {
                return (<div key={'div_' + i}>
                  <h3 style={{fontWeight: 'bold', marginBottom: '10px'}}>有效记录
                    &nbsp;
                    <Icon
                      key={'record' + i}
                      type={this.state.visibility[i] === 'none' ? 'down-circle-o' : 'up-circle-o'}
                      onClick={() => {
                        var v = this.state.visibility;
                        if (v[i] === 'none') {
                          v[i] = 'block';
                        } else {
                          v[i] = 'none';
                        }
                        this.setState({
                          visibility: v,
                        });
                      }}
                    />
                  </h3>
                  <Row key={'rowd_' + i} type="flex" justify="start" style={{marginLeft: '40px', marginTop: '20px', width: '700px'}}>
                    <Col style={{color: 'gray', display: this.state.visibility[i]}}>
                      {
                        this.state.details[i].map((r, j) => {
                          return (
                            <span key={'spand_' + i + '_' + j}>{r}<br/></span>
                          );
                        })
                      }
                    </Col>
                  </Row>
                </div>);

              }
            )
          )}

        </div>
      </div>
    );
  }
}
