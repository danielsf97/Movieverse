import React, { Component } from 'react';
import Form from 'react-bootstrap/Form'
import Button from 'react-bootstrap/Button'
import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import InputGroup from 'react-bootstrap/InputGroup'
import Datepicker from '../datepicker'
import ReactFlagsSelect from 'react-flags-select'

import 'react-flags-select/css/react-flags-select.css';

export default class Register extends Component  {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
    this.handleButton = this.handleButton.bind(this)
    this.handleSubmit = this.handleSubmit.bind(this)
    this.handleCountry = this.handleCountry.bind(this)
    this.state = {
      email: "",
      username: "",
      password: "",
      name: "",
      birthdate: new Date(),
      country: "PT",
      gender: "Female",
      validated: false
    }
  }

  handleChange(e) {
    let m = {}
    m[e.target.name] = e.target.value
    this.setState(m)
  }

  handleCountry(c) {
    this.setState({country: c})
  }

  handleButton(e) {
    this.setState({validated: true})
  }

  handleSubmit(e) {
    let m = this.state
    delete m.validated
    this.setState({validated: false})
    this.props.register(m)
    e.preventDefault();
  }

  render() {
    return (
      <Form onSubmit={this.handleSubmit} validated={this.state.validated}>
        <InputGroup>
          <InputGroup.Prepend>
            <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-at"></i></InputGroup.Text>
          </InputGroup.Prepend>
          <Form.Control
            name="username"
            type="text"
            placeholder="Username" 
            aria-describedby="inputGroupPrepend" 
            pattern=".{3,30}" 
            title="Username must have between 3 and 30 chars"
            onChange={this.handleChange}
            required
          />
        </InputGroup>
        <p/>
        <InputGroup>
          <InputGroup.Prepend>
            <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-envelope"></i></InputGroup.Text>
          </InputGroup.Prepend>
          <Form.Control
            name="email"
            type="email" 
            placeholder="Email" 
            onChange={this.handleChange}
            required
          />
        </InputGroup>
        <p/>
        <InputGroup>
          <InputGroup.Prepend>
            <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-user"></i></InputGroup.Text>
          </InputGroup.Prepend>
          <Form.Control
            name="name"
            type="text" 
            placeholder="Name" 
            pattern=".{3,50}" 
            title="Name must have between 3 and 50 chars"
            onChange={this.handleChange}
            required
          />
        </InputGroup>
        <p/>
        <InputGroup>
          <InputGroup.Prepend>
            <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-key"></i></InputGroup.Text>
          </InputGroup.Prepend>
          <Form.Control
            name="password"
            type="password" 
            placeholder="Password"
            pattern=".{8,30}" 
            title="Password must have between 8 and 30 chars" 
            onChange={this.handleChange}
            required
          />
        </InputGroup>
        <p/>
        <InputGroup>
          <InputGroup.Prepend>
            <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-key"></i></InputGroup.Text>
          </InputGroup.Prepend>
          <Form.Control 
            name="cpassword"
            type="password" 
            placeholder="Confirm Password"
            pattern={this.state.password}
            title="Password must match"
            required
          />
        </InputGroup>
        <p/>
        <InputGroup style={{ 'marginBottom':'0.7em' }}>
          <table>
            <tbody>
              <tr>
              <td>
                <InputGroup.Prepend>
                  <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-birthday-cake"></i></InputGroup.Text>
                </InputGroup.Prepend>
              </td>
              <td>
                <Datepicker 
                  selected={this.state.birthdate}
                  change={this.handleChange}
                />
              </td>            
            </tr>
          </tbody>
        </table>
        </InputGroup>
        <Row className="margin-bottom-10">
          <Col xs="12" sm="6" style={{ 'marginBottom':'0.7em' }}>
          <div className="left-right">
            <span className="label-country">Country</span>
            <ReactFlagsSelect
              name="country"
              searchable={true}
              defaultCountry="PT"
              searchPlaceholder="Country"
              showSelectedLabel={false}
              onSelect={this.handleCountry}
            />
          </div>
          </Col>
          <Col xs="12" sm="6">
            <InputGroup>
              <InputGroup.Prepend>
                <InputGroup.Text id="inputGroupPrepend"><i className="fas fa-venus-mars"></i></InputGroup.Text>
              </InputGroup.Prepend>
              <Form.Control 
                as="select" 
                className="no-validator" 
                name="gender"
                onChange={this.handleChange}
                value={this.state.gender}>
                <option>Female</option>
                <option>Male</option>
                <option>Other</option>
              </Form.Control>
            </InputGroup>
          </Col>
        </Row>
        <Row>
          <Col md="8" xs="6">
            <div className="error-message">
              {this.props.registerFail ? this.props.errorMessage : ""}
            </div>
          </Col>
          <Col>
            <Button 
              className="float-right" 
              variant="secondary" 
              type="submit" 
              onClick={this.handleButton}>
              Submit
            </Button>
          </Col>
        </Row>
      </Form>
    )
  }
}
