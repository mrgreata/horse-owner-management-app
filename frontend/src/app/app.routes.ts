import { Routes } from '@angular/router';
import { HorseCreateEditComponent, HorseCreateEditMode } from './component/horse/horse-create-edit/horse-create-edit.component';
import { HorseComponent } from './component/horse/horse.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'horses' },

  // Horses
  {
    path: 'horses',
    children: [
      { path: '', component: HorseComponent },
      { path: 'create', component: HorseCreateEditComponent, data: { mode: HorseCreateEditMode.create } },
      {
        path: ':id',
        loadComponent: () =>
          import('./component/horse/horse-detail/horse-detail.component')
            .then(m => m.HorseDetailComponent)
      },
      { path: ':id/edit', component: HorseCreateEditComponent, data: { mode: HorseCreateEditMode.edit } },
      {
        path: ':id/tree',
        loadComponent: () =>
          import('./component/horse/horse-tree/horse-tree')
            .then(m => m.HorseTreeComponent)
      },
    ]
  },

  // Owners (US7)
  // src/app/app.routes.ts
  {
    path: 'owners',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./component/owner/owner-list/owner-list.component')
            .then(m => m.OwnerListComponent)
      },
      {
        path: 'create',
        loadComponent: () =>
          import('./component/owner/owner-create/owner-create.component')
            .then(m => m.OwnerCreateComponent)
      },
    ]
  },


  { path: '**', redirectTo: 'horses' },
];
