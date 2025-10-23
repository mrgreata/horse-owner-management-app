import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";
import {NgClass} from "@angular/common";
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
//import {Horse} from 'src/app/dto/horse';
import { CommonModule } from '@angular/common';
import { Horse } from 'src/app/dto/horse';





export enum HorseCreateEditMode {
  create,
  edit
}



@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  imports: [
    CommonModule,
    FormsModule,
    AutocompleteComponent,
    NgClass,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {
  motherSelection: Horse | null = null;
  fatherSelection: Horse | null = null;

  Sex = Sex;
  HorseCreateEditMode = HorseCreateEditMode;
  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    owner: null as Owner | null,
  };
  horseBirthDateIsSet = false;


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public get heading(): string {
    return this.mode === HorseCreateEditMode.create ? 'Create New Horse' : 'Edit Horse';
  }
  public get submitButtonText(): string {
    return this.mode === HorseCreateEditMode.create ? 'Create' : 'Save';
  }
  private get modeActionFinished(): string {
    return this.mode === HorseCreateEditMode.create ? 'created' : 'updated';
  }


  /*
  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      default:
        return '?';
    }
  }
   */

  public get horseBirthDateText(): string {
    if (!this.horseBirthDateIsSet) {
      return '';
    } else {
      return formatIsoDate(this.horse.dateOfBirth);
    }
  }

  public set horseBirthDateText(date: string) {
    if (date == null || date === '') {
      this.horseBirthDateIsSet = false;
    } else {
      this.horseBirthDateIsSet = true;
      this.horse.dateOfBirth = new Date(date);
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }


  get sex(): string {
    switch (this.horse.sex) {
      case Sex.male:
        return 'Male';
      case Sex.female:
        return 'Female';
      default:
        return '';
    }
  }

/*
  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      default:
        return '?';
    }
  }
*/
  ownerSuggestions = (input: string): Observable<Owner[]> =>
    input.trim() === '' ? of([]) : this.ownerService.searchByName(input, 5);

// Wrapper nur fürs Template, damit die Template-Typprüfung zufrieden ist:
  readonly ownerSuggestionsForAuto =
    (input: string): Observable<never[]> =>
      this.ownerSuggestions(input) as unknown as Observable<never[]>;

  motherSuggestions = (input: string): Observable<Horse[]> => {
    if (input.trim() === '') return of([]);
    return this.service.search({
      name: input,
      sex: Sex.female,
      bornBefore: this.horseBirthDateIsSet ? this.horse.dateOfBirth : undefined,
      limit: 5
    });
  };

  fatherSuggestions = (input: string): Observable<Horse[]> => {
    if (input.trim() === '') return of([]);
    return this.service.search({
      name: input,
      sex: Sex.male,
      bornBefore: this.horseBirthDateIsSet ? this.horse.dateOfBirth : undefined,
      limit: 5
    });
  };


  public formatHorseLabel(h: Horse | null | undefined): string {
    return h ? `${h.name} (${formatIsoDate(h.dateOfBirth)})` : '';
  }
  readonly parentFormatter = (h: Horse | null | undefined) => this.formatHorseLabel(h);

  private preloadParentsIfAny(): void {
    if (this.horse.motherId) {
      this.service.getById(this.horse.motherId).subscribe(m => this.motherSelection = m);
    }
    if (this.horse.fatherId) {
      this.service.getById(this.horse.fatherId).subscribe(f => this.fatherSelection = f);
    }
  }


// Wrapper für das <app-autocomplete>
  readonly motherSuggestionsForAuto =
    (input: string): Observable<never[]> =>
      this.motherSuggestions(input) as unknown as Observable<never[]>;

  readonly fatherSuggestionsForAuto =
    (input: string): Observable<never[]> =>
      this.fatherSuggestions(input) as unknown as Observable<never[]>;



  ngOnInit(): void {
    // Modus direkt aus den Route-Daten lesen (siehe app.routes.ts)
    this.route.data.subscribe(d => {
      this.mode = d['mode'] ?? HorseCreateEditMode.create;

      if (this.modeIsEdit) {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.service.getById(id).subscribe({
          next: horse => {
            this.horse = horse;
            this.horseBirthDateIsSet = true;
            this.preloadParentsIfAny();
          },
          error: err => {
            this.notification.error(
              this.errorFormatter.format(err),
              'Could Not Load Horse',
              { enableHtml: true, timeOut: 10000 }
            );
            this.router.navigate(['/horses']);
          }
        });
      }
    });

    // Falls man innerhalb der Komponente zu einem anderen /:id/edit navigiert:
    this.route.paramMap.subscribe(pm => {
      if (this.modeIsEdit) {
        const id = Number(pm.get('id'));
        if (!Number.isNaN(id)) {
          this.service.getById(id).subscribe({
            next: horse => {
              this.horse = horse;
              this.horseBirthDateIsSet = true;
              this.preloadParentsIfAny();
            },
            error: err => {
              this.notification.error(
                this.errorFormatter.format(err),
                'Could Not Load Horse',
                { enableHtml: true, timeOut: 10000 }
              );
              this.router.navigate(['/horses']);
            }
          });
        }
      }
    });
  }



  get modeIsEdit(): boolean {
    return this.mode === HorseCreateEditMode.edit;
  }


  public dynamicCssClassesForInput(input: NgModel, form?: NgForm): any {
    return {
      'is-invalid': input.invalid && (input.dirty || input.touched || !!form?.submitted),
    };
  }


  public formatOwnerName(owner: Owner | null | undefined): string {
    return owner ? `${owner.firstName} ${owner.lastName}` : '';
  }
  readonly ownerFormatter = (o: Owner | null | undefined) => this.formatOwnerName(o);

  onOwnerSelected(owner: Owner | null) {
    this.horse.owner = owner;
  }


  public deleteFromEdit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.delete(id).subscribe({
      next: () => {
        this.notification.success(`Horse ${this.horse.name} deleted.`);
        this.router.navigate(['/horses']);
      },
      error: err => {
        this.notification.error(this.errorFormatter.format(err), 'Could Not Delete Horse', { enableHtml: true, timeOut: 10000 });
      }
    });
  }



  public onSubmit(form: NgForm): void {
    if (!form.valid) {
      return;
    }

    if (this.horse.description === '') {
      delete this.horse.description;
    }

    let observable: Observable<Horse>;

    switch (this.mode) {
      case HorseCreateEditMode.create: {
        observable = this.service.create({
          name: this.horse.name,
          description: this.horse.description,
          dateOfBirth: this.horse.dateOfBirth,
          sex: this.horse.sex,
          ownerId: this.horse.owner?.id ?? null,
          motherId: this.motherSelection?.id ?? null,
          fatherId: this.fatherSelection?.id ?? null,
        });
        break;
      }
      case HorseCreateEditMode.edit: {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        observable = this.service.update(id, {
          name: this.horse.name,
          description: this.horse.description,
          dateOfBirth: this.horse.dateOfBirth,
          sex: this.horse.sex,
          ownerId: this.horse.owner?.id ?? null,
          motherId: this.motherSelection?.id ?? null,
          fatherId: this.fatherSelection?.id ?? null,
        });
        break;
      }
      default:
        console.error('Unknown HorseCreateEditMode', this.mode);
        return;
    }

    if (this.horse.owner && !this.horse.owner.id) {
      this.notification.error('Please select an owner from suggestions.');
      return;
    }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error creating horse', error);
          this.notification.error(this.errorFormatter.format(error), 'Could Not Create Horse', {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
    }
}
