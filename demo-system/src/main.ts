import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { Chart, registerables } from 'chart.js';

import { AppModule } from './app/app.module';

// Register Chart.js components
Chart.register(...registerables);

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
