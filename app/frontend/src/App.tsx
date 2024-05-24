import {Box, Grid} from '@mui/material';
import SearchBar from './components/SearchBar';

import {createTheme} from '@mui/material/styles';
import {ThemeProvider} from '@emotion/react';
import SimilarProducts from './api/SimilarProducts';
import {useState} from 'react';

export const theme = createTheme({
  palette: {
    primary: {
      main: '#16496e',
      dark: '#092a41',
      light: '#99d3fab1',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#e78414',
      light: '#f3a849',
      dark: '#a55900',
      contrastText: '#ffffff',
    },
    info: {
      main: '#ffffff',
    },
    background: {
      paper: '#DEF6FE',
    },
  },
});

function App() {
  const [imageUrl, setImageUrl] = useState<string>('');
  const handleSearch = (imageUrl: string) => {
    console.log('Handling search with image URL: ', imageUrl);
    setImageUrl(imageUrl);
  };

  return (
    <ThemeProvider theme={theme}>
      <Box
        sx={{
          backgroundColor: 'background.paper',
          height: '100vh',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Grid container spacing={4} sx={{flexGrow: 1}}>
          <Grid item xs={12}>
            <SearchBar onSearch={handleSearch} />
          </Grid>
          <Grid item xs={12} sx={{flex: 1, overflowY: 'auto'}}>
            <SimilarProducts imageUrl={imageUrl}></SimilarProducts>
          </Grid>
        </Grid>
      </Box>
    </ThemeProvider>
  );
}

export default App;
