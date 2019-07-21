import * as React from 'react';
import * as Style from '../Style';
import { Icon } from 'antd';

interface ExpandableDivProps {
  style?: any;
  maxHeight?: number;
}

interface ExpandableDivState {
  enable: boolean;
  expanded: boolean;
  maxHeight: number;
}

export default class ExpandableDiv extends React.Component<ExpandableDivProps, ExpandableDivState> {
  content: any;

  constructor(props: ExpandableDivProps) {
    super(props);
    this.state = {
      enable: true,
      expanded: false,
      maxHeight: this.props.maxHeight || 400,
    };
  }

  componentDidMount() {
    this.setState({enable: this.content.clientHeight > this.state.maxHeight});
  }

  render() {
    return (
      <div style={this.props.style}>
        <div style={{position: 'relative'}}>
          <div
            style={
              this.state.enable ? (
                this.state.expanded
                  ? Style.showDiv
                  : {...Style.foldDiv, maxHeight: `${this.state.maxHeight}px`}
              ) : {}
            }
          >
            <div ref={(elem) => this.content = elem}>
              {this.props.children}
            </div>
          </div>
          {(this.state.enable && !this.state.expanded) && (
            <div
              style={{
                position: 'absolute',
                width: '100%',
                height: '100%',
                top: 0,
                backgroundImage: 'linear-gradient(180deg,rgba(255,255,255,0) 50%,#fff 100%)',
              }}
            />
          )}
        </div>
        {this.state.enable && (
          <div
            style={{cursor: 'pointer', textAlign: 'center'}}
            onClick={() => this.setState({expanded: !this.state.expanded})}
          >
            {this.state.expanded ? <Icon type="up"/> : <Icon type="down"/>}
          </div>
        )}
      </div>
    );
  }
}
