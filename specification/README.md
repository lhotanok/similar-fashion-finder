## Web application for searching fashion products by images

### Description

The goal of this project is to create a simple web application where users can provide a link to a fashion product from an arbitrary website. The image will then be analyzed and compared to the images extracted from 3 different e-shops. We'll be primarily looking for the same products but similar products will be accepted as well. The project should cover women's and men's fashion (children's fashion will be added later) on the following websites, with Czech localizations only:

- [Zalando](https://www.zalando.cz/)
- [About You](https://www.aboutyou.cz/)
- [ZOOT](https://www.zoot.cz/)

For each extracted image, we'll calculate its fingerprint and store it in a database along with the links of corresponding product detail pages and with a simple product description. We'll work with several image hashing methods such as perceptive hash, color hash, average hash, wavelet hash or difference hash.

Once we discover a matching product based on the provided image link, we'll fetch up-to-date information about product's price and return it in the response along with the matched image and general product description. We won't store product prices in our database since they can change quite often and we don't have resources to crawl the whole websites accordingly. 

### Technologies

#### Data extraction

- TypeScript
- [Crawlee](https://github.com/apify/crawlee)
- [Apify SDK](https://github.com/apify/apify-sdk-js)

#### Image hashing

- Java
- [JImageHash](https://github.com/KilianB/JImageHash)

#### Database

- SQL for image matching by hashes (as illustrated in JImageHash [DatabaseExample](https://github.com/KilianB/JImageHash-Examples/blob/main/src/main/java/dev/brachtendorf/jimagehash_examples/DatabaseExample.java))
- MongoDB for product details (with image hashes for identification)

#### Web application

##### Backend

- Java
- [JImageHash](https://github.com/KilianB/JImageHash) for image matching

##### Frontend

- TypeScript
- [React](https://reactjs.org/)
- [Material UI](https://mui.com/)

### Image hashing algorithms

Let's take a look at the overview of available image hashing algorithms briefly to see how they could be integrated with our application. We'll assign hashes to the following images using the [ImageHash](https://pypi.org/project/ImageHash/) Python library. Code was taken from the article '[How to use identify visually similar images using hashing](https://practicaldatascience.co.uk/data-science/how-to-use-image-hashing-to-identify-visually-similar-or-duplicate-images)'.

![images-for-hashes](D:\OneDrive - Univerzita Karlova\Dokumenty\Skola\MFF_UK\Rocnik_4\Java\Zapoctovy_program\images-for-hashes.png)

The following grid contains hashes from all hashing functions mentioned above:

- average hash - `ahash`
- perceptive hash - `phash`
- difference hash - `dhash`
- wavelet hash - `whash` 
- color hash - `colorhash`

![image-hashes-grid](D:\OneDrive - Univerzita Karlova\Dokumenty\Skola\MFF_UK\Rocnik_4\Java\Zapoctovy_program\image-hashes-grid.png)

From this brief example, color hash seems to be most efficient in terms of identifying the same images. It correctly recognized that the images with indices 0 and 1 are the same, as well as the images 7 and 8. Even though the images are cropped and thus not 100% same, it handled a different size and translation well. However, it doesn't perform that well with monochromatic images such as 4 and 6.  Their color hashes seem to be closer than hashes of images 4 and 5, even when 4 and 5 show the same item and 4 and 6 show a completely different item. 

We can see that using just the color hash alone wouldn't be reliable enough. But we can definitely combine it with other hash functions and mark the product as matching only when it complies to multiple restrictions. A distance of two hashes can be measured e.g. by hamming distance. Our restrictions on matching products may be something like: 

1. hamming distance of color hashes must be less than 4 
2. hamming distance of average hashes must be less than 10

When we apply those rules and try to find matching products for the image on index 5, we'll get of course image 5 as it is the same image but also image 4, whose `ahash` is within hamming distance 0-10 and whose `colorhash` is at hamming distance equal to 3. Image 6 has even smaller `colorhash` distance equal to 2 but it won't be matched as the distance of its `ahash` is above the limit of 10 with its value 16.

![hamming-ahash](D:\OneDrive - Univerzita Karlova\Dokumenty\Skola\MFF_UK\Rocnik_4\Java\Zapoctovy_program\hamming-ahash.png)![hamming-colorhash](D:\OneDrive - Univerzita Karlova\Dokumenty\Skola\MFF_UK\Rocnik_4\Java\Zapoctovy_program\hamming-colorhash.png)

### Scale

#### Total products

- [Zalando](https://www.zalando.cz/): ~ 400,000 items
- [About You](https://www.aboutyou.cz/): ~250,000 items
- [ZOOT](https://www.zoot.cz/): ~ 55,000 items

Crawling the whole websites would provide us with ~700,000 products. We'll start with a POC and limit the number of products extracted from each website to 10,000. We'll also skip children's fashion in POC phase and extract 5,000 products from both women's and men's fashion categories.

#### Total products for POC

- [Zalando](https://www.zalando.cz/): ~ 10,000 items
- [About You](https://www.aboutyou.cz/): ~10,000 items
- [ZOOT](https://www.zoot.cz/): ~ 10,000 items

### Extensions

In the first phase, we'll be trying to achieve similar functionality as [Google Lens](https://lens.google.com/) provides, only being limited on the field of fashion products and 3 websites. 

The next step would be creating a browser extension(s) that would allow product search triggering directly by right-clicking any image on the web. Users would then be redirected to the web application to browse the matched products. This approach is used e.g. by [TinEye](https://tineye.com/extensions):

![tin-eye](D:\OneDrive - Univerzita Karlova\Dokumenty\Skola\MFF_UK\Rocnik_4\Java\Zapoctovy_program\tin-eye.png)

Ultimately, it would be nice to recommend product alternatives injected in the source page directly next to the currently viewed product. It would make the whole process of finding the same item for better price much easier and faster than Google Lens or TinEye allow at the moment. Products injection to the webpage could be inspired by another browser extension -  [Hlídač shopů](https://www.hlidacshopu.cz/).