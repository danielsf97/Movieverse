import React, { Component } from 'react'

export default class MovieCard extends Component {
  render() {
    return (
      <div className="text-center movie-card-container">
        <img className="movie-card" src={this.props.img} alt="" />
        <div className="label">
          <p>{this.props.title}</p>
          <p className="small">{this.props.info}</p>
        </div>
      </div>
    )
  }
}