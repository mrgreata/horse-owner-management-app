import {Routes} from '@angular/router';

import {
  HorseCreateEditComponent,
  HorseCreateEditMode
} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseDetailComponent} from './component/horse/horse-detail/horse-detail.component'; // 👈 NEU importieren





export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'horses' },  // <— hinzufügen
  {
    path: 'horses', children: [
      { path: '', component: HorseComponent },
      { path: 'create', component: HorseCreateEditComponent, data: { mode: HorseCreateEditMode.create } },
      {
        path: ':id',
        loadComponent: () =>
          import('./component/horse/horse-detail/horse-detail.component')
            .then(m => m.HorseDetailComponent)
      },
      { path: ':id/edit', component: HorseCreateEditComponent, data: { mode: HorseCreateEditMode.edit } },

    ]
  },
  { path: '**', redirectTo: 'horses' },
];


