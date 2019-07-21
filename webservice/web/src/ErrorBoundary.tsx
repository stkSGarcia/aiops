import * as React from 'react';

interface ErrorBoundaryProps {
  redirect?: string;
}

interface ErrorBoundaryState {
  hasError: boolean;
}

export default class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: any) {
    super(props);
    this.state = {hasError: false};
  }

  componentDidCatch(error: any, info: any) {
    this.setState({hasError: true});
    console.log(error, info);
    if (this.props.redirect) {
      window.location.replace(this.props.redirect);
    } else {
      window.location.replace('/');
    }
  }

  render() {
    if (this.state.hasError) {
      return <div/>;
    }
    return this.props.children;
  }
}
