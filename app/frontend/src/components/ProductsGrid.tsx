import Grid from '@mui/material/Grid';
import Container from '@mui/material/Container';
import ProductCard from './ProductCard';
import { Product } from '../types/Product';

export default function ProductsGrid({ products }: { products: Product[] }) {
  return (
    <Container>
      <Grid container spacing={4} justifyContent='center'>
        {products.map((product, index) => {
          const oneOflastTwoCards =
            index !== products.length - 1 && index !== products.length - 2;

            console.log(`Creating grid item for product: ${product.id}`);

          return (
            <Grid
              item
              key={product.id}
              xs={products.length % 3 !== 2 ? true : oneOflastTwoCards ? true : false}
            >
              <ProductCard product={product} />
            </Grid>
          );
        })}
      </Grid>
    </Container>
  );
}