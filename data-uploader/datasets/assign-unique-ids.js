const fs = require('fs');
const { v5: uuidv5 } = require('uuid');

const readFiles = (dirname, onFileContent, onError) => {
    fs.readdir(dirname, (err, filenames) => {
        if (err) {
            onError(err);
            return;
        }

        filenames.forEach((filename) => {
            fs.readFile(dirname + filename, 'utf-8', (err, content) => {
                if (err) {
                    onError(err);
                    return;
                }
                onFileContent(filename, content);
            });
        });
    });
}

const injectIdsToFiles = (filesDirectory) => {
    const NAMESPACE = 'f47ac10b-58cc-4372-a567-0e02b2c3d479';

    readFiles(`${__dirname}/${filesDirectory}/`, (filename, content) => {
        const products = JSON.parse(content);
        const productsWithIds = {};

        products.forEach((product) => {
            const id = uuidv5(product.url, NAMESPACE);

            productsWithIds[id] = {
                id: null,
                ...product,
                id,
            };
        });

        fs.writeFileSync(
            `${__dirname}/${filesDirectory}/${filename}`,
            JSON.stringify(Object.values(productsWithIds), null, 2),
        );
    }, (err) => {
        throw err;
    });
}

injectIdsToFiles('data/zalando');
injectIdsToFiles('data/zoot');
