import axios from 'axios'

const baseURL = 'http://localhost:8081'

const request = axios.create({
  baseURL,
})

export const test = () => request('test/hello')



