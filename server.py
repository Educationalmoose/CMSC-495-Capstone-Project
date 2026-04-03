from flask import Flask, request, jsonify
import tensorflow as tf

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def square_matrix():
    """Processes the incoming JSON data, performs matrix multiplication, and returns the result."""
    try: 
        # catch the byte stream from the request
        raw_bytes = request.get_data()

        # convert the stream of bytes into a tensor and decode it as a PNG image, which will give us a 2D matrix with a single channel (grayscale)
        matrix = tf.io.decode_png(raw_bytes, channels=1)

        # resize the matrix to 28x28 pixels
        matrix = tf.image.resize(matrix, [28, 28])

        # normalize the pixel values to be between 0 and 1
        matrix = matrix / 255.0

        # expand the dimensions of the matrix to add a batch dimension, which is required for input into a neural network model
        final_input = tf.expand_dims(matrix, axis=0)



        # -------------------------------------------------------------------------
        # prediction model analyzes the final_input tensor and outputs it's guesses
        # -------------------------------------------------------------------------



        # just for testing, we'll convert the tensor back to an image and save it to disk to verify that the input is being processed correctly
        debug_image = tf.squeeze(final_input, axis=0) 
        debug_image = debug_image * 255.0
        debug_image = tf.cast(debug_image, tf.uint8)
        new_png = tf.io.encode_png(debug_image)
        tf.io.write_file('new_image.png', new_png)

        # just for testing purposes, we'll perform matrix multiplication on the input tensor with itself and return the result
        # return the result of matrix multiplication as a JSON response
        return jsonify({
                "status": "success",
                "message": "Matrix received and processed",
                "new_matrix": str(final_input)
            }), 200
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
            }), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)