import numpy as np
import tensorflow as tf
from tensorflow import keras
from PIL import Image
import io
import matplotlib.cm as cm

#this should be the model.h5 path
from os.path import dirname, join
image_path = join(dirname(__file__), 'image.jpeg')
model_path = join(dirname(__file__), 'model.h5')

def test(a):
    return str(np.array([1, 2, 3]))+str(tf.__version__)+a

def make_gradcam_heatmap(
        img_array, model, last_conv_layer_name, classifier_layer_names):
    # First, we create a model that maps the input image to the activations
    # of the last conv layer
    last_conv_layer = model.get_layer(last_conv_layer_name)
    last_conv_layer_model = keras.Model(model.inputs, last_conv_layer.output)

    # Second, we create a model that maps the activations of the last conv
    # layer to the final class predictions
    classifier_input = keras.Input(shape=last_conv_layer.output.shape[1:])
    x = classifier_input
    for layer_name in classifier_layer_names:
        x = model.get_layer(layer_name)(x)
    classifier_model = keras.Model(classifier_input, x)

    # Then, we compute the gradient of the top predicted class for our input image
    # with respect to the activations of the last conv layer
    with tf.GradientTape() as tape:
        # Compute activations of the last conv layer and make the tape watch it
        last_conv_layer_output = last_conv_layer_model(img_array)
        tape.watch(last_conv_layer_output)
        # Compute class predictions
        preds = classifier_model(last_conv_layer_output)
        top_pred_index = tf.argmax(preds[0])
        top_class_channel = preds[:, top_pred_index]

    # This is the gradient of the top predicted class with regard to
    # the output feature map of the last conv layer
    grads = tape.gradient(top_class_channel, last_conv_layer_output)

    # This is a vector where each entry is the mean intensity of the gradient
    # over a specific feature map channel
    pooled_grads = tf.reduce_mean(grads, axis=(0, 1, 2))

    # We multiply each channel in the feature map array
    # by "how important this channel is" with regard to the top predicted class
    last_conv_layer_output = last_conv_layer_output.numpy()[0]
    pooled_grads = pooled_grads.numpy()
    for i in range(pooled_grads.shape[-1]):
        last_conv_layer_output[:, :, i] *= pooled_grads[i]

    # The channel-wise mean of the resulting feature map
    # is our heatmap of class activation
    heatmap = np.mean(last_conv_layer_output, axis=-1)

    # For visualization purpose, we will also normalize the heatmap between 0 & 1
    heatmap = np.maximum(heatmap, 0) / np.max(heatmap)
    return heatmap

def pass_image(byteArray):
    img_size = (224, 224)

    img = Image.open(io.BytesIO(byteArray))                 #catch the bytearray from .java, create image from bytearray
    img = img.convert('RGB')
    img = img.resize(img_size, Image.NEAREST)               #resize
    img = keras.preprocessing.image.img_to_array(img)     #convert to np array
    array = np.expand_dims(img, axis=0)

    #do somethin with np array
    #code your gradcam here
    model = tf.keras.models.load_model(model_path)
    preprocess_input = keras.applications.xception.preprocess_input

    last_conv_layer_name = "block14_sepconv2_act"
    classifier_layer_names = [
        "avg_pool2d",
        "flatten",
        "first_dense",
        "dropout",
        "second_dense"
    ]

    img_array = preprocess_input(array)

    # Generate class activation heatmap
    heatmap = make_gradcam_heatmap(
        img_array, model, last_conv_layer_name, classifier_layer_names
    )

    # We rescale heatmap to a range 0-255
    heatmap = np.uint8(255 * heatmap)

    # We use jet colormap to colorize heatmap
    jet = cm.get_cmap("jet")

    # We use RGB values of the colormap
    jet_colors = jet(np.arange(256))[:, :3]
    jet_heatmap = jet_colors[heatmap]

    # We create an image with RGB colorized heatmap
    jet_heatmap = keras.preprocessing.image.array_to_img(jet_heatmap)
    jet_heatmap = jet_heatmap.resize((img.shape[1], img.shape[0]))
    jet_heatmap = keras.preprocessing.image.img_to_array(jet_heatmap)

    gmb = Image.open(io.BytesIO(byteArray))                 #catch the bytearray from .java, create image from bytearray
    gmb = gmb.convert('RGB')
    gmb = gmb.resize(img_size, Image.NEAREST)               #resize
    gmb = keras.preprocessing.image.img_to_array(gmb)     #convert to np array

    # Superimpose the heatmap on original image
    superimposed_img = jet_heatmap * 0.3 + gmb
    superimposed_img = keras.preprocessing.image.array_to_img(superimposed_img)

    #img = Image.fromarray(np.uint8(superimposed_img)).convert('RGB')   #create image from np array
    imgByteArr = io.BytesIO()
    superimposed_img.save(imgByteArr, format='JPEG')
    imgByteArr = imgByteArr.getvalue()                      #convert to byte array again
    return imgByteArr