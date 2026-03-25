from flask import Flask, request, jsonify
import numpy as np
app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def square_matrix():
    """Processes the incoming JSON data, performs matrix multiplication, and returns the result."""

    # Extract the JSON data from the request
    incoming_data = request.get_json()

    # convert the incoming matrix data to a numpy array
    matrix = np.array(incoming_data.get('matrix', []))

    # return the result of matrix multiplication as a JSON response
    return jsonify((matrix @ matrix).tolist()), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)