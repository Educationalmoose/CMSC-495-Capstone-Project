import requests

# payload represents the JSON data that we're sending to the flask server for analysis
payload = {
    "matrix": [
        [2, 4, 6],
        [6, 8, 10],
        [10, 12, 14]
    ]
}

# send a POST request to the flask server with the payload and print the response
response = requests.post("http://127.0.0.1:5000/predict", json=payload)

print(response.json())