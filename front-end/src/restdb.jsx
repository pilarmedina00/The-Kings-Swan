// Use relative paths so Vite dev server proxy (vite.config.js) will forward to
// the backend services in development. This also makes the app work in prod
// behind a reverse proxy.
const custBaseURL = '/api/customers';
const authBaseUrl = '/account';

// Stored JWT token for requests to the Data Service. This is updated when
// the user logs in successfully.
let token = null;


/* CUSTOMER REQUESTS */

let getHeaders = (token) => {
  const myHeaders = new Headers({ "Content-Type": "application/json" });
  if (token != null && token !== undefined) {
    myHeaders.append("Authorization", "Bearer " + token);
  }
  return myHeaders;
}

export async function getAll(setCustomers) {
  const myInit = {
    method: 'GET',
    mode: 'cors',
    headers: getHeaders()
  };
  const fetchData = async (url) => {
    try {
      const response = await fetch(url, myInit);
      if (!response.ok) {
        throw new Error(`Error fetching data: ${response.status}`);
      }
      const data = await response.json();
      setCustomers(data);
    } catch (error) {
      alert(error);
    }
  }
  fetchData(custBaseURL);
}

export async function deleteById(id, postopCallback) {
  const myInit = {
    method: 'DELETE',
    mode: 'cors',
    headers: getHeaders()
  };
  const deleteItem = async (url) => {
    try {
      const response = await fetch(url, myInit);
      if (!response.ok) {
        throw new Error(`Error deleting data: ${response.status}`);
      }
      postopCallback();
    } catch (error) {
      alert(error);
    }
  }
  deleteItem(custBaseURL + "/" + id);
}

export function post(customer, postopCallback) {
  delete customer.id;
  const myInit = {
    method: 'POST',
    body: JSON.stringify(customer),
    headers: getHeaders(),
    mode: 'cors'
  };
  const postItem = async (url) => {
    try {
      const response = await fetch(url, myInit);
      if (!response.ok) {
        throw new Error(`Error posting data: ${response.status}`);
      }
      postopCallback();
    } catch (error) {
      alert(error);
    }
  }
  postItem(custBaseURL);
}

export function put(customer, postopCallback) {
  const myInit = {
    method: 'PUT',
    body: JSON.stringify(customer),
    headers: getHeaders(),
    mode: 'cors'
  };
  const putItem = async (url) => {
    try {
      const response = await fetch(url, myInit);
      if (!response.ok) {
        throw new Error(`Error puting data: ${response.status}`);
      }
      postopCallback();
    } catch (error) {
      alert(error);
    }
  }
  putItem(custBaseURL + "/" + customer.id);
}

export function lookupCustomerByName(username) {
  var myInit = {
    method: 'POST',
    body: username,
    headers: getHeaders(),
    mode: 'cors'
  };
  const lookupCustomer = async (url) => {
    try {
      const response = await fetch(url, init);
      if (!response.ok) {
        throw new Error(`Error looking up customer: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      alert(error);
    }
  };
  lookupCustomer(custBaseURL + "/byname");
}


/* LOGIN REQUESTS */
export async function registerUser( username, password, email) {
  let url = authBaseUrl +"/register";
  let customer = {
    name: username,
    email: email,
    password: password
  }
  let body = JSON.stringify(customer);
  var myInit = {
    method: 'POST',
    body: body,
    headers: getHeaders(),
    mode: 'cors'
  };
  try {
    const response = await fetch(url, myInit);
    if (!response.ok) {
      throw new Error(`Error registering user: ${response.status}`);
    }
    const text = await response.text();
    return text;
  } catch (error) {
    alert(error);
  }
}


export function callTokenService(credentials) {
  let url = authBaseUrl + "/token";
  // credentials: { username, password }
  let body = JSON.stringify({ username: credentials.username, password: credentials.password });
  var myInit = {
    method: 'POST',
    body: body,
    headers: getHeaders(),
    mode: 'cors'
  };
  return fetch(url, myInit);
}



export async function getJWTToken(username, password) {
  // Send { username, password } to /token
  const response = await callTokenService({ username, password });
  if (!response.ok) {
    return { status: "error", message: "Login failed: " + response.status };
  }
  // Expect JSON { token: ... }
  const data = await response.json();
  // store the token for future requests
  token = data.token;
  return { status: "success", message: "Login successful", token: data.token };
}


