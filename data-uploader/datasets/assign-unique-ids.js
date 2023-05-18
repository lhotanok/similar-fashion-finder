const fs = require('fs');
const { v5: uuidv5 } = require('uuid');

function readFiles(dirname, onFileContent, onError) {
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

const NAMESPACE = 'f47ac10b-58cc-4372-a567-0e02b2c3d479';

readFiles(`${__dirname}/data/`, (filename, content) => {
    const products = JSON.parse(content);
    const productsWithIds = products.map((product) => ({
        id: uuidv5(product.url, NAMESPACE),
        ...product,
    }));

    fs.writeFileSync(
        `${__dirname}/data/${filename}`,
        JSON.stringify(productsWithIds, null, 2),
    );
}, (err) => {
    throw err;
});
