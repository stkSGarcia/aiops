import * as React from 'react';
import * as moment from 'moment';
import { TimePicker } from 'antd';

interface TimeRangePickerProps {
  value?: { from: number, to: number };
  style?: React.CSSProperties;
  onChange?: any;
}

interface TimeRangePickerState {
  from: number;
  to: number;
}

export default class TimeRangePicker extends React.Component<TimeRangePickerProps, TimeRangePickerState> {
  normalize = (value: number): number => {
    // time range per day: -28800000 ~ 57600000
    while (value > 57600000) {
      value -= 86400000;
    }
    while (value < -28800000) {
      value += 86400000;
    }
    return value;
  }

  onFromChange = (time) => {
    if (time !== null) {
      const from = time.valueOf();
      this.setState({from});
      this.triggerChange({from});
    }
  }

  onToChange = (time) => {
    if (time !== null) {
      const to = time.valueOf();
      this.setState({to});
      this.triggerChange({to});
    }
  }

  triggerChange = (changedValue) => {
    const onChange = this.props.onChange;
    if (onChange) {
      onChange(Object.assign({}, this.state, changedValue));
    }
  }

  constructor(props: TimeRangePickerProps) {
    super(props);
    if (props.value === undefined) {
      this.state = {from: -28800000, to: -28800000};
    } else {
      this.state = {from: this.normalize(props.value.from), to: this.normalize(props.value.to)};
    }
  }

  componentWillReceiveProps(nextProps: TimeRangePickerProps) {
    if (nextProps.value !== undefined) {
      this.setState({from: nextProps.value.from, to: nextProps.value.to});
    }
  }

  render() {
    return (
      <div style={this.props.style}>
        <TimePicker value={moment(this.state.from)} onChange={this.onFromChange}/>
        &nbsp;&nbsp;~&nbsp;&nbsp;
        <TimePicker value={moment(this.state.to)} onChange={this.onToChange}/>
      </div>
    );
  }
}
