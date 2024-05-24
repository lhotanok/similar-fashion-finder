# Similar Fashion Finder
Web application for searching fashion products by images 👗📷

## Installation

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) v20 or higher
- [Docker Compose](https://docs.docker.com/compose/install/) v2.13 or higher
- [Docker Desktop](https://docs.docker.com/desktop/) (optionally)

## Run

### Databases

The application uses 2 databases internally:

- **MongoDB** for rich product data
- **H2 Database** for image hashes (used internally by [JImageHash](https://github.com/KilianB/JImageHash) library)

Mongo database will be configured with Docker Compose. Simply run the following command from `app` directory:

`docker-compose up -d`

This command will run Docker Compose in a detached mode and it will set up MongoDB and its corresponding web interface Mongo Express. Web interface will be accessible on its default location and port:

- Mongo Express
  - Database `products`: `http://localhost:8081/mongo-express/db/products/`
  - Default credentials: `admin` username, `admin` password

### Backend

Backend code is gathered in `app/backend` directory. You'll find a single Java console application that is responsible for both data upload and serving the same data through the API endpoint: `http://localhost:4567/imageMatcher`. This endpoint accepts GET requests with 1 query parameter: `imageUrl`. This parameter should hold an encoded URL of an image that should be used for product similarity search.

The application can be built and run with Maven using the corresponding commands directly or through an IDE such as IntelliJ IDEA or Eclipse.

The process of data uploading can take longer based on the amount of products you choose to upload. JSON files with fashion products from Zalando and Zoot websites are expected to be stored in `backend/src/main/resources/datasets/`  in `zalando` and `zoot` subdirectories. There're already a few product examples to make the application running quickly. More products can be found in `app/backend/datasets/data` directory. The latest data can be extracted using the open source Apify actors:

- [Zalando Scraper](https://apify.com/lhotanova/zalando-scraper)
- [Zoot Scraper](https://apify.com/lhotanova/zoot-scraper)

### Frontend

Frontend code is stored in `app/frontend` directory. It is a single page React application where users can provide image URL and this URL is used as a parameter in search for same or at least similar products.

The application is accessible on `http://localhost:3000` and it is deployed with `docker-compose up -d` command along with MongoDB and backend application. It can also be started individually by running `npm start` from the `app/frontend` directory. 
