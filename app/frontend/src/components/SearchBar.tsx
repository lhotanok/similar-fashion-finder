import {
  AppBar,
  Avatar,
  Box,
  Grid,
  IconButton,
  TextField,
  Toolbar,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import {theme} from '../App';
import { useState } from 'react';

export default function SearchBar({ onSearch }: { onSearch: (imageUrl: string) => void}) {
  const [imageUrl, setImageUrl] = useState<string>('');

  return (
    <Box sx={{flexGrow: 1}}>
      <AppBar position='static' sx={{paddingTop: 2}}>
        <Toolbar sx={{margin: 2}}>
          <Grid container alignItems='center'>
            <Grid item xs={1}>
              <Avatar alt='Similar Fashion Finder' src='/static/images/logo.png' />
            </Grid>
            <Grid item xs={4}>
              <Typography
                variant='h5'
                noWrap
                component='div'
                sx={{
                  flexGrow: 1,
                  display: {xs: 'none', sm: 'block'},
                }}
              >
                Similar Fashion Finder
              </Typography>
            </Grid>
            <Grid item xs={7} alignItems='center'>
              <Box display='flex' alignItems='center' width='100%'>
                <IconButton sx={{background: 'transparent'}} onClick={() => onSearch(imageUrl)}>
                  <SearchIcon color='info' />
                </IconButton>
                <TextField
                  label='Image URL'
                  variant='outlined'
                  color='secondary'
                  fullWidth
                  sx={{marginLeft: 1}}
                  onInput={(e) => setImageUrl((e.target as HTMLInputElement).value)}
                  InputProps={{
                    style: {color: theme.palette.primary.contrastText},
                  }}
                  InputLabelProps={{
                    style: {color: theme.palette.primary.light},
                  }}
                />
              </Box>
            </Grid>
          </Grid>
        </Toolbar>
      </AppBar>
    </Box>
  );
}
