import Card from '@mui/material/Card';
import {useRef} from 'react';
import {Product} from '../types/Product';
import {Box, CardActionArea, CardContent, CardMedia, Tooltip, Typography} from '@mui/material';

export default function ProductCard({product}: {product: Product}) {
  const cardRef = useRef<HTMLDivElement>(null);

  console.log(`Rendering ProductCard for product: ${product.id}`, {product});

  return (
    <Card
      ref={cardRef}
      elevation={5}
      sx={{
        maxWidth: 350,
        backgroundColor: 'primary.light',
        margin: 'auto',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
      }}
    >
        <Tooltip title={product.url} arrow>
      <CardActionArea onClick={() => window.open(product.url, '_blank')}>
        <Box sx={{display: 'flex', flexDirection: 'column', height: '100%'}}>
          <CardContent sx={{flex: '1 0 auto'}}>
            <Typography component='div' variant='h6'>
              {product.name}
            </Typography>
            <Typography variant='subtitle1' color='text.secondary' component='div'>
              Available sizes: {parseAvailableSizes(product).join(', ')}
            </Typography>
          </CardContent>
          <Box sx={{display: 'flex', alignItems: 'center', pl: 2, pb: 1}}>
            <Typography variant='subtitle2' color='secondary.dark' fontWeight='bold'>
              {parsePrice(product)}
            </Typography>
          </Box>
          <CardMedia
            component='img'
            sx={{maxHeight: '50vh', objectFit: 'cover', objectPosition: '20% 20%'}}
            image={product.thumbnail}
            alt={product.name}
          />
        </Box>
      </CardActionArea></Tooltip>
    </Card>
  );
}

const parsePrice = (product: Product): string => {
  if ('price' in product) {
    return `${product.price.current} ${product.priceCurrency}`;
  } else if ('currentBestPrice' in product) {
    return `${product.currentBestPrice.formattedPrice}`;
  }
  return 'Unknown price';
};

const parseAvailableSizes = (product: Product): string[] => {
  const availableSizes: string[] = [];

  for (const size of product.sizes) {
    if ('available' in size && size.available) {
      availableSizes.push(size.size);
    } else if ('stockStatus' in size && size.stockStatus !== 'OUT_OF_STOCK') {
      availableSizes.push(size.size);
    }
  }

  return availableSizes;
};
