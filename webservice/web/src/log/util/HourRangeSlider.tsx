import * as React from 'react';
import * as moment from 'moment';
import { Slider } from 'antd';

interface HourRangeSliderProps {
  date: number;
  range: [number, number];
  style?: React.CSSProperties;
  onChange?: any;
}

interface HourRangeSliderState {
  min: number;
  max: number;
  range: [number, number];
}

export default class HourRangeSlider extends React.Component<HourRangeSliderProps, HourRangeSliderState> {
  viewRange = (start: number, end: number): [number, number] => {
    const startHour = moment(start).hour();
    const endMoment = moment(end);
    const endHour = endMoment.minute() > 0 ? endMoment.hour() + 1 : endMoment.hour();
    return [startHour, endHour];
  }

  marks = (start: number, end: number) => {
    const mark = {};
    for (let i = start; i <= end; i++) {
      mark[i] = i;
    }
    return mark;
  }

  // onRangeMove = (value) => {
  //   const oldValue = this.state.range;
  //   if (value[0] === oldValue[0] || value[0] === oldValue[1]) {
  //     if (value[1] - value[0] !== 1) {
  //       value = [value[1] - 1, value[1]];
  //     }
  //   } else if (value[1] === oldValue[0] || value[1] === oldValue[1]) {
  //     if (value[1] - value[0] !== 1) {
  //       value = [value[0], value[0] + 1];
  //     }
  //   }
  //   this.setState({range: value});
  // }
  //
  // onRangeChange = (value) => {
  //   const min = this.props.date + value[0] * 3600000 - 28800000;
  //   const max = this.props.date + value[1] * 3600000 - 28800000;
  //   this.props.onChange([min, max]);
  // }

  onRangeMove = (value) => {
    if (value[0] !== value[1]) {
      this.setState({range: [value[0], value[1]]});
    }
  }

  onRangeChange = (value) => {
    this.setState({range: value});
    const min = this.props.date + value[0] * 3600000 - 28800000;
    const max = this.props.date + value[1] * 3600000 - 28800000;
    if (this.props.onChange !== undefined) {
      this.props.onChange([min, max]);
    }
  }

  constructor(props: HourRangeSliderProps) {
    super(props);
    const range = this.viewRange(props.range[0], props.range[1]);
    this.state = {
      min: range[0],
      max: range[1],
      range: range,
    };
  }

  componentWillReceiveProps(nextProps: HourRangeSliderProps) {
    if (nextProps.range !== undefined) {
      const range = this.viewRange(nextProps.range[0], nextProps.range[1]);
      this.setState({
        min: range[0],
        max: range[1],
        range: range,
      });
    }
  }

  render() {
    return (
      <div style={this.props.style}>
        <Slider
          value={this.state.range}
          min={this.state.min}
          max={this.state.max}
          step={1}
          marks={this.marks(this.state.min, this.state.max)}
          range={true}
          onChange={this.onRangeMove}
          onAfterChange={this.onRangeChange}
        />
      </div>
    );
  }

  // render() {
  //   const marks = {
  //     0: '0h',
  //     3: '3h',
  //     6: '6h',
  //     9: '9h',
  //     12: '12h',
  //     15: '15h',
  //     18: '18h',
  //     21: '21h',
  //     24: '24h'
  //   };
  //   return (
  //     <div style={this.props.style}>
  //       <Slider
  //         value={this.state.range}
  //         min={0}
  //         max={24}
  //         step={1}
  //         marks={marks}
  //         range={true}
  //         onChange={this.onRangeMove}
  //         onAfterChange={this.onRangeChange}
  //       />
  //     </div>
  //   );
  // }
}
