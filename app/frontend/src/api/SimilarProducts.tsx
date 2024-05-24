import { useState, useEffect } from 'react';
import axios from 'axios';
import { Product } from '../types/Product';
import { Typography } from '@mui/material';
import ProductsGrid from '../components/ProductsGrid';
import { SimilarProductResponse } from '../types/SimilarProductResponse';

const SimilarProducts = ({ imageUrl }: { imageUrl: string }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchSimilarProducts = async () => {
      const serverPort = process.env.BACKEND_PORT || '4567';

      console.log(
        `Sending request to http://localhost:${serverPort}/imageMatcher with imageUrl: ${imageUrl}`,
      );

      try {
        const response = await axios.get(`http://localhost:${serverPort}/imageMatcher`, {
          params: {
            imageUrl: imageUrl,
          },
        });
        const responseData = response.data as SimilarProductResponse[];
        setProducts(responseData.map(({ product }) => product));
        console.log(`Set ${response.data.length} products from response`);
      } catch (err) {
        setError(err as Error);
      }
    };

    fetchSimilarProducts();
  }, [imageUrl]);

  if (error) return <Typography>Error: {error.message}</Typography>;

  return <ProductsGrid products={products} />;
};

export default SimilarProducts;
