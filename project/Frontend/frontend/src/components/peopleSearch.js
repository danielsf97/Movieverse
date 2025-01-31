import React, { Component } from 'react';
import Form from 'react-bootstrap/Form'
import Container from 'react-bootstrap/Container'
import InputGroup from 'react-bootstrap/InputGroup'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import Jumbotron from 'react-bootstrap/Jumbotron'
import MovieCard from './movie-card'
import Button from 'react-bootstrap/Button'
import Axios from 'axios'
import {backend, labels} from '../var'
import NoResultsFound from './aux_pages/noResultsFound';
import InfiniteScroller from 'react-infinite-scroller'


export default class PeopleSearch extends Component {
  constructor(props) {
    super(props)
    this.state = {
      name: undefined,
      nameTimeout: 0,
      results: undefined,
      bornToday: [],
      mostCredits: [],
      bornTodayCurrent: 1,
      mostCreditsCurrent: 1,
      cards: [],
      next: 1
    }
    this.handleName = this.handleName.bind(this)
    this.search = this.search.bind(this)
    this.handleShowMore = this.handleShowMore.bind(this)
    this.loadMore = this.loadMore.bind(this)

  }

  componentDidMount() {
    document.title = "People Search | Movieverse"
    this.getInfo()
  }

  handleName(e) {
    if (this.state.nameTimeout) {
      clearTimeout(this.state.nameTimeout)
    }

    this.setState({
      name: e.target.value,
      nameTimeout: setTimeout(x => this.search(), 300)
    })
  }

  handleShowMore(type) {
    if (type === 'bornToday')
      this.setState({
        bornTodayCurrent: this.state.bornTodayCurrent + 1
      })
    
    else
      this.setState({
        mostCreditsCurrent: this.state.mostCreditsCurrent + 1
      })
  }

  getInfo() {
    Axios.get(backend + '/member/search-page').then(x => {
      this.setState({
        bornToday: x.data.bornToday,
        mostCredits: x.data.mostCredits
      })
    })
  }

  buildCards(people, isSearch, i) {
    let l = []
    let p

    if (isSearch) {
      p = people.slice(18 * (i - 1), 18 * i)
    }
    else {
      p = people
    }
    
    p.forEach(x => {
      let info
      if (x.total)
        info = x.total + ' ' + labels[this.props.lang].credits
      else if (x.age)
        info = x.age + ' ' + labels[this.props.lang].years

      l.push(
        <Col lg="2" md="3" xs="4" key={people.indexOf(x)}>
          <br></br>
          <MovieCard small
            img={'http://image.tmdb.org/t/p/w154/' + x.image}
            title={x.name}
            member={x.id}
            info={info}
          />
        </Col>
      )
    })

    return l
  }

  search() {
    let query = '?name=' + this.state.name
    Axios.get(backend + '/member/search' + query).then(x => {
      this.setState({
        results: x.data,
        cards: [],
        next: 1
      })
    })
  }

  loadMore() {
    const cards = this.state.cards.concat(this.buildCards(this.state.results, true, this.state.next))
    this.setState({
      cards: cards,
      next: this.state.next + 1,
    })
  }

  render() {
    let to_render

    if (this.state.results) {
      if (this.state.results.length === 0) {
        to_render =
          <Container className="container-padding">
            <NoResultsFound lang={this.props.lang} />
          </Container>
      }
      else {
        to_render = 
          <Container className="container-padding">
            <Row>
              { /* this.buildCards(this.state.results) */}
              <InfiniteScroller
                pageStart={0}
                loadMore={this.loadMore}
                hasMore={this.state.cards.length < this.state.results.length}>
                <Row>
                  {this.state.cards}
                </Row>
              </InfiniteScroller>
            </Row>
          </Container>
      }
    }

    else to_render = 
      <>
      <Container className="container-padding">
        <div className="title-medium">
          {labels[this.props.lang].bornToday}
          </div>
        <Row>
          {this.buildCards(this.state.bornToday.slice(0, this.state.bornTodayCurrent * 6))}
        </Row>
        {this.state.bornTodayCurrent < 18 ?
          <Button variant="secondary" size="sm" className="button-slim"
            onClick={() => this.handleShowMore('bornToday')}>
            {labels[this.props.lang].showMore}
          </Button>
          : ""}
      </Container>
      <Container className="container-padding">
        <div className="title-medium">
          {labels[this.props.lang].peopleMostCredits}
          </div>
        <Row>
          {this.buildCards(this.state.mostCredits.slice(0, this.state.mostCreditsCurrent * 6))}
        </Row>
        {this.state.mostCreditsCurrent < 18 ?
          <Button variant="secondary" size="sm" className="button-slim"
            onClick={() => this.handleShowMore('mostCredits')}>
            {labels[this.props.lang].showMore}
          </Button>
          : ""}
      </Container>
      </>

    return (
      <>
        <Jumbotron fluid>
          <Container className="text-center">
            <h1 className="jumbotron-text">{labels[this.props.lang].peopleSearch}</h1>
            <Row>
              <Col md={{ span: 6, offset: 3 }}>
                <InputGroup className="input-margin">
                  <InputGroup.Prepend>
                    <InputGroup.Text id="inputGroupPrepend" className="bg-light-gray">
                      <i className="fas fa-search" />
                    </InputGroup.Text>
                  </InputGroup.Prepend>
                  <Form.Control
                    className="search-input"
                    type="text"
                    name="title"
                    placeholder={labels[this.props.lang].name}
                    onChange={this.handleName}
                  />
                </InputGroup>
              </Col>
            </Row>
          </Container>
        </Jumbotron>
        {to_render}
      </>
    )
  }
}