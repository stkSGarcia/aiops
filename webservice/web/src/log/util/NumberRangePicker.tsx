import * as React from 'react';
import { InputNumber } from 'antd';

interface NumberRangePickerProps {
  value?: { min: number | undefined, max: number | undefined };
  style?: React.CSSProperties;
  onChange?: any;
}

interface NumberRangePickerState {
  min: number | undefined;
  max: number | undefined;
}

export default class NumberRangePicker extends React.Component<NumberRangePickerProps, NumberRangePickerState> {
  onMinChange = (min: number) => {
    this.setState({min});
    this.triggerChange({min});
  }

  onMaxChange = (max: number) => {
    this.setState({max});
    this.triggerChange({max});
  }

  triggerChange = (changedValue) => {
    const onChange = this.props.onChange;
    if (onChange) {
      onChange(Object.assign({}, this.state, changedValue));
    }
  }

  constructor(props: NumberRangePickerProps) {
    super(props);
    if (props.value === undefined) {
      this.state = {min: undefined, max: undefined};
    } else {
      this.state = {min: props.value.min, max: props.value.max};
    }
  }

  componentWillReceiveProps(nextProps: NumberRangePickerProps) {
    if (nextProps.value !== undefined) {
      this.setState({min: nextProps.value.min, max: nextProps.value.max});
    }
  }

  render() {
    return (
      <div style={this.props.style}>
        <InputNumber value={this.state.min} min={0} onChange={this.onMinChange}/> ms
        &nbsp;~&nbsp;
        <InputNumber value={this.state.max} min={0} onChange={this.onMaxChange}/> ms
      </div>
    );
  }
}
