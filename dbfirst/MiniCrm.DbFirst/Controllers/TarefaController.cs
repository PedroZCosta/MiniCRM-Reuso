using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class TarefaController : Controller
    {
        private readonly MiniCrmContext _context;

        public TarefaController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: Tarefa
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.Tarefa.Include(t => t.IdClienteNavigation).Include(t => t.IdOportunidadeNavigation).Include(t => t.IdUsuarioNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: Tarefa/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var tarefa = await _context.Tarefa
                .Include(t => t.IdClienteNavigation)
                .Include(t => t.IdOportunidadeNavigation)
                .Include(t => t.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdTarefa == id);
            if (tarefa == null)
            {
                return NotFound();
            }

            return View(tarefa);
        }

        // GET: Tarefa/Create
        public IActionResult Create()
        {
            ViewData["IdCliente"] = new SelectList(_context.Cliente, "IdCliente", "Email");
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa");
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email");
            return View();
        }

        // POST: Tarefa/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdTarefa,Concluida,ConcluidaEm,CriadoEm,DataVencimento,Descricao,Titulo,IdCliente,IdOportunidade,IdUsuario")] Tarefa tarefa)
        {
            if (ModelState.IsValid)
            {
                _context.Add(tarefa);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdCliente"] = new SelectList(_context.Cliente, "IdCliente", "Email", tarefa.IdCliente);
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa", tarefa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", tarefa.IdUsuario);
            return View(tarefa);
        }

        // GET: Tarefa/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var tarefa = await _context.Tarefa.FindAsync(id);
            if (tarefa == null)
            {
                return NotFound();
            }
            ViewData["IdCliente"] = new SelectList(_context.Cliente, "IdCliente", "Email", tarefa.IdCliente);
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa", tarefa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", tarefa.IdUsuario);
            return View(tarefa);
        }

        // POST: Tarefa/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("IdTarefa,Concluida,ConcluidaEm,CriadoEm,DataVencimento,Descricao,Titulo,IdCliente,IdOportunidade,IdUsuario")] Tarefa tarefa)
        {
            if (id != tarefa.IdTarefa)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(tarefa);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!TarefaExists(tarefa.IdTarefa))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdCliente"] = new SelectList(_context.Cliente, "IdCliente", "Email", tarefa.IdCliente);
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa", tarefa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", tarefa.IdUsuario);
            return View(tarefa);
        }

        // GET: Tarefa/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var tarefa = await _context.Tarefa
                .Include(t => t.IdClienteNavigation)
                .Include(t => t.IdOportunidadeNavigation)
                .Include(t => t.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdTarefa == id);
            if (tarefa == null)
            {
                return NotFound();
            }

            return View(tarefa);
        }

        // POST: Tarefa/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var tarefa = await _context.Tarefa.FindAsync(id);
            if (tarefa != null)
            {
                _context.Tarefa.Remove(tarefa);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool TarefaExists(int id)
        {
            return _context.Tarefa.Any(e => e.IdTarefa == id);
        }
    }
}
